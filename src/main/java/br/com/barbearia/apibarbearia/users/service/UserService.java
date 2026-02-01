package br.com.barbearia.apibarbearia.users.service;

import br.com.barbearia.apibarbearia.availability.service.AvailabilityService;
import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import br.com.barbearia.apibarbearia.common.exception.ConflictException;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.notification.email.users.UserEmailNotificationService;
import br.com.barbearia.apibarbearia.users.dto.*;
import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.entity.UserInvite;
import br.com.barbearia.apibarbearia.users.repository.UserInviteRepository;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserInviteRepository inviteRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailNotificationService emailNotificationService;
    private final AvailabilityService availabilityService;

    // =========================================================
    // LISTAGEM E CONSULTA
    // =========================================================

    public List<UserResponse> list() {
        Role actor = currentRole();

        if (actor == Role.DEV) {
            return userRepository.findAll()
                    .stream().map(this::toResponse)
                    .collect(Collectors.toList());
        }
        if (actor == Role.ADMIN) {
            return userRepository.findAll().stream()
                    .filter(user -> user.getRole() != Role.DEV)
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }
        // Staff vê outros Staffs
        return userRepository.findAllByRole(Role.STAFF)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        User target = getUser(id);
        Role actor = currentRole();

        if (actor == Role.ADMIN && target.getRole() == Role.DEV) {
            throw new BadRequestException("Administrador não pode visualizar usuários DEV.");
        }
        return toResponse(target);
    }

    // =========================================================
    // CONVITES
    // =========================================================

    @Transactional
    public void inviteUser(InviteUserRequest req) {
        Role actor = currentRole();
        String email = normalizeEmail(req.getEmail());

        if (actor == Role.ADMIN && req.getRole() != Role.STAFF) {
            throw new BadRequestException("Administradores só podem convidar Staff.");
        }

        if (userRepository.existsByEmailAndActiveTrue(email)) {
            throw new ConflictException("Já existe um usuário ativo com este e-mail.");
        }

        // Limpeza de usuários inativos antigos com mesmo e-mail
        userRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.isActive()) {
                userRepository.delete(existing);
                userRepository.flush();
            }
        });

        // Verifica convites pendentes
        List<UserInvite> existingInvites = inviteRepository.findAllByEmail(email);
        boolean hasValidInvite = existingInvites.stream()
                .anyMatch(inv -> !inv.isUsed() && inv.getExpiresAt().isAfter(Instant.now()));

        if (hasValidInvite) {
            throw new ConflictException("Já existe um convite pendente e válido para este e-mail.");
        }

        if (!existingInvites.isEmpty()) {
            inviteRepository.deleteAll(existingInvites);
            inviteRepository.flush();
        }

        String token = UUID.randomUUID().toString();
        UserInvite invite = UserInvite.builder()
                .email(email)
                .role(req.getRole())
                .token(token)
                .used(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        inviteRepository.save(invite);

        try {
            emailNotificationService.sendInvite(email, token);
        } catch (Exception e) {
            inviteRepository.delete(invite);
            throw new BadRequestException("Erro ao enviar e-mail de convite.");
        }
    }

    public UserInvite validateInviteToken(String token) {
        UserInvite invite = inviteRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Convite inválido ou não encontrado."));

        if (invite.isUsed()) {
            throw new BadRequestException("Este convite já foi utilizado.");
        }

        if (Instant.now().isAfter(invite.getExpiresAt())) {
            throw new BadRequestException("O prazo de 24h deste convite expirou.");
        }

        return invite;
    }

    // =========================================================
    // CADASTRO FINAL (Completa o Convite)
    // =========================================================

    @Transactional
    public UserResponse completeInvite(CompleteInviteRequest req) {
        UserInvite invite = validateInviteToken(req.getToken());

        // Validação do Nome (sem números)
        String validName = validateAndNormalizeName(req.getName());

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new BadRequestException("As senhas não conferem.");
        }
        if (req.getPassword().length() < 5) {
            throw new BadRequestException("A senha deve ter no mínimo 5 caracteres.");
        }

        if (userRepository.existsByEmailAndActiveTrue(invite.getEmail())) {
            throw new ConflictException("Este e-mail já foi cadastrado.");
        }

        String cleanPhone = null;
        if (req.getPhone() != null && !req.getPhone().isBlank()) {
            cleanPhone = req.getPhone().replaceAll("[^0-9]", "");
            validatePhoneUniqueness(cleanPhone, null);
        }

        // Remove resquícios se houver
        userRepository.findByEmail(invite.getEmail()).ifPresent(u -> {
            userRepository.delete(u);
            userRepository.flush();
        });

        User newUser = User.builder()
                .name(validName)
                .email(invite.getEmail())
                .phone(cleanPhone)
                .role(invite.getRole())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .active(true)
                .mustChangePassword(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(newUser);

        // ✅ CRIAÇÃO AUTOMÁTICA DA AGENDA (Segunda a Sábado, 08h-18h, Domingo inativo)
        System.out.println("🔧 Criando agenda padrão para usuário ID: " + savedUser.getId());
        try {
            availabilityService.createDefaultSchedule(savedUser.getId());
            System.out.println("✅ Agenda criada com sucesso para user " + savedUser.getId());
        } catch (Exception e) {
            System.err.println("❌ ERRO ao criar agenda: " + e.getMessage());
            e.printStackTrace();
            // Não lançar exceção para não quebrar o cadastro do usuário
        }

        invite.setUsed(true);
        inviteRepository.save(invite);

        try {
            emailNotificationService.sendUserCreated(savedUser.getEmail(), savedUser.getName());
        } catch (Exception ignored) {}

        return toResponse(savedUser);
    }

    // =========================================================
    // ATUALIZAÇÃO
    // =========================================================

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest req) {
        Role actor = currentRole();
        User target = getUser(id);
        String email = normalizeEmail(req.getEmail());

        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new BadRequestException("Este e-mail já está em uso.");
        }

        if (actor == Role.ADMIN && target.getRole() == Role.DEV) {
            throw new BadRequestException("Sem permissão para editar DEV.");
        }

        if (actor == Role.ADMIN && req.getRole() != Role.STAFF) {
            throw new BadRequestException("Administrador não pode elevar privilégios para outros cargos.");
        }

        if (req.getPhone() != null) {
            String cleanPhone = req.getPhone().replaceAll("[^0-9]", "");
            if (!cleanPhone.equals(target.getPhone())) {
                validatePhoneUniqueness(cleanPhone, target.getId());
                target.setPhone(cleanPhone);
            }
        }

        String validName = validateAndNormalizeName(req.getName());

        boolean updatedBySelf = currentUserId() != null && currentUserId().equals(target.getId());

        target.setName(validName);
        target.setEmail(email);
        target.setUpdatedAt(Instant.now());

        if (actor == Role.DEV) {
            target.setRole(req.getRole());
        }

        User saved = userRepository.save(target);

        try {
            if (updatedBySelf) {
                emailNotificationService.sendUserUpdatedBySelf(saved.getEmail(), saved.getName());
            } else {
                User adminUser = getUser(currentUserId());
                emailNotificationService.sendUserUpdatedByAdmin(
                        saved.getEmail(), saved.getName(), saved.getEmail(),
                        saved.getRole().name(), adminUser.getEmail(), actor.name()
                );
            }
        } catch (Exception ignored) {}

        return toResponse(saved);
    }

    // =========================================================
    // ADMINISTRAÇÃO (Delete / Activate / Reset)
    // =========================================================

    @Transactional
    public UserResponse deleteAndReturn(Long id) {
        Role actor = currentRole();
        User target = getUser(id);

        if (actor == Role.ADMIN && target.getRole() != Role.STAFF) {
            throw new BadRequestException("Administrador só pode excluir STAFF.");
        }

        String targetEmail = target.getEmail();
        String targetName = target.getName();
        User adminUser = getUser(currentUserId());

        userRepository.delete(target);

        try {
            emailNotificationService.sendUserDeletedByAdmin(targetEmail, targetName, adminUser.getEmail());
        } catch (Exception ignored) {}

        return toResponse(target);
    }

    @Transactional
    public UserResponse activateAndReturn(Long id) {
        Role actor = currentRole();
        User target = getUser(id);

        if (actor == Role.ADMIN && target.getRole() != Role.STAFF) {
            throw new BadRequestException("Administrador só pode ativar STAFF.");
        }

        target.setActive(true);
        target.setUpdatedAt(Instant.now());
        return toResponse(userRepository.save(target));
    }

    @Transactional
    public UserWithTempPasswordResponse resetPassword(Long id) {
        Role actor = currentRole();
        User target = getUser(id);

        if (actor == Role.ADMIN && target.getRole() != Role.STAFF) {
            throw new BadRequestException("Sem permissão.");
        }

        String temp = generateTempPassword(10);
        target.setPasswordHash(passwordEncoder.encode(temp));
        target.setMustChangePassword(true);
        target.setTokenInvalidationTimestamp(Instant.now());
        userRepository.save(target);

        try {
            User adminUser = getUser(currentUserId());
            emailNotificationService.sendPasswordResetByAdmin(
                    target.getEmail(), target.getName(), temp, adminUser.getEmail()
            );
        } catch (Exception ignored) {}

        return wrapWithTemp(target, temp);
    }

    // =========================================================
    // RECUPERAÇÃO DE SENHA (Flow Público)
    // =========================================================

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest req) {
        String email = normalizeEmail(req.getEmail());
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) throw new NotFoundException("Usuário não encontrado.");
        User user = userOptional.get();
        if (!user.isActive()) throw new BadRequestException("Usuário inativo.");

        String code = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
        user.setEmailVerificationCode(code);
        user.setEmailVerificationExpiresAt(Instant.now().plusSeconds(300));
        userRepository.save(user);

        try {
            emailNotificationService.sendPasswordResetCode(user.getEmail(), user.getName(), code);
        } catch (Exception ignored) {}
    }

    public void validateRecoveryCode(String email, String code) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        if (user.getEmailVerificationCode() == null || !user.getEmailVerificationCode().equals(code)) {
            throw new BadRequestException("Código inválido.");
        }
        if (Instant.now().isAfter(user.getEmailVerificationExpiresAt())) {
            throw new BadRequestException("Código expirado.");
        }
    }

    @Transactional
    public void completePasswordReset(CompletePasswordResetRequest req) {
        User user = userRepository.findByEmail(normalizeEmail(req.getEmail()))
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        validateRecoveryCode(req.getEmail(), req.getCode());

        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            throw new BadRequestException("Senhas não conferem.");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Nova senha não pode ser igual à atual.");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setMustChangePassword(false);
        user.setEmailVerificationCode(null);
        user.setTokenInvalidationTimestamp(Instant.now());
        userRepository.save(user);
    }

    // =========================================================
    // MEU PERFIL (Self Update)
    // =========================================================

    @Transactional
    public UserResponse updateMyProfile(User user, UpdateMyProfileRequest req) {
        String currentEmail = user.getEmail();
        String newEmailRaw = req.getEmail().toLowerCase().trim();

        String validName = validateAndNormalizeName(req.getName());

        boolean nameChanged = !user.getName().equals(validName);
        boolean emailChanged = !currentEmail.equalsIgnoreCase(newEmailRaw);

        String newPhone = req.getPhone() != null ? req.getPhone().replaceAll("[^0-9]", "") : null;
        String oldPhone = user.getPhone();
        boolean phoneChanged = false;

        if (newPhone != null && !newPhone.isBlank()) {
            if (!newPhone.equals(oldPhone)) {
                validatePhoneUniqueness(newPhone, user.getId());
                user.setPhone(newPhone);
                phoneChanged = true;
            }
        }

        if (nameChanged) user.setName(validName);

        if (emailChanged) {
            if (userRepository.existsByEmailAndIdNot(newEmailRaw, user.getId())) {
                throw new ConflictException("E-mail já em uso.");
            }
            String code = String.valueOf(new SecureRandom().nextInt(900000) + 100000);
            user.setPendingEmail(newEmailRaw);
            user.setEmailVerificationCode(code);
            user.setEmailVerificationExpiresAt(Instant.now().plusSeconds(300));
            try {
                emailNotificationService.sendVerificationCode(newEmailRaw, user.getName(), code);
            } catch (Exception ignored) {}
        }

        user.setUpdatedAt(Instant.now());
        User saved = userRepository.save(user);

        if ((nameChanged || phoneChanged) && !emailChanged) {
            try {
                emailNotificationService.sendUserUpdatedBySelf(saved.getEmail(), saved.getName());
            } catch (Exception ignored) {}
        }

        return toResponse(saved);
    }

    @Transactional
    public User confirmEmailUpdate(User user, String code) {
        if (user.getPendingEmail() == null || user.getEmailVerificationCode() == null ||
                !user.getEmailVerificationCode().equals(code)) {
            throw new BadRequestException("Código inválido ou sem solicitação.");
        }
        if (Instant.now().isAfter(user.getEmailVerificationExpiresAt())) {
            throw new BadRequestException("Código expirado.");
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setEmailVerificationCode(null);
        user.setEmailVerificationExpiresAt(null);
        user.setTokenInvalidationTimestamp(Instant.now());

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Senha atual incorreta.");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Nova senha deve ser diferente da atual.");
        }
        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            throw new BadRequestException("Confirmação de senha incorreta.");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        user.setMustChangePassword(false);
        user.setTokenInvalidationTimestamp(Instant.now());
        userRepository.save(user);

        try {
            emailNotificationService.sendPasswordChanged(user.getEmail(), user.getName());
        } catch (Exception ignored) {}
    }

    // =========================================================
    // HELPERS E VALIDAÇÕES
    // =========================================================

    private void validatePhoneUniqueness(String phone, Long userIdToIgnore) {
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        if (cleanPhone.isEmpty()) return;

        Optional<User> existingUser = userRepository.findByPhone(cleanPhone);
        if (existingUser.isPresent()) {
            if (userIdToIgnore != null && existingUser.get().getId().equals(userIdToIgnore)) {
                return;
            }
            throw new ConflictException("Este número de telefone já está em uso por outro usuário.");
        }
    }

    private String validateAndNormalizeName(String name) {
        if (name == null || name.trim().length() < 3) {
            throw new BadRequestException("O nome é obrigatório e deve ter no mínimo 3 caracteres.");
        }
        if (name.matches(".*\\d.*")) {
            throw new BadRequestException("O nome não pode conter números.");
        }
        return name.trim();
    }

    private User getUser(Long id) {
        if (id == null) throw new BadRequestException("Sessão inválida.");
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase().trim();
    }

    private Role currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new BadRequestException("Sessão inválida.");
        boolean isDev = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEV"));
        if (isDev) return Role.DEV;
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return Role.ADMIN;
        return Role.STAFF;
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try { return Long.parseLong(auth.getName()); } catch (Exception e) { return null; }
    }

    private UserResponse toResponse(User user) {
        UserResponse r = new UserResponse();
        r.setId(user.getId());
        r.setName(user.getName());
        r.setEmail(user.getEmail());
        r.setPhone(user.getPhone());
        r.setRole(user.getRole());
        r.setActive(user.isActive());
        r.setMustChangePassword(user.isMustChangePassword());
        r.setCreatedAt(user.getCreatedAt());
        r.setUpdatedAt(user.getUpdatedAt());
        r.setPendingEmail(user.getPendingEmail());
        return r;
    }

    private UserWithTempPasswordResponse wrapWithTemp(User user, String temp) {
        UserWithTempPasswordResponse resp = new UserWithTempPasswordResponse();
        resp.setUser(toResponse(user));
        resp.setTemporaryPassword(temp);
        return resp;
    }

    private String generateTempPassword(int length) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}