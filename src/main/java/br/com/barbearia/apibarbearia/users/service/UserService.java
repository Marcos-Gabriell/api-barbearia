package br.com.barbearia.apibarbearia.users.service;

import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import br.com.barbearia.apibarbearia.common.exception.ConflictException;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.notification.email.users.UserEmailNotificationService;
import br.com.barbearia.apibarbearia.users.dtos.*;
import br.com.barbearia.apibarbearia.users.entity.Role.Role;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailNotificationService emailNotificationService;

    public List<UserResponse> list() {
        Role actor = currentRole();

        if (actor == Role.DEV) {
            return userRepository.findAll()
                    .stream().map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return userRepository.findAllByRole(Role.STAFF)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        User target = getUser(id);
        Role actor = currentRole();

        if (actor == Role.ADMIN && target.getRole() != Role.STAFF) {
            throw new BadRequestException("Administrador só pode visualizar usuários do tipo STAFF.");
        }

        return toResponse(target);
    }

    public UserWithTempPasswordResponse create(UserCreateRequest req) {
        Role actor = currentRole();
        String email = normalizeEmail(req.email);

        if (email == null || email.isBlank()) {
            throw new BadRequestException("E-mail é obrigatório.");
        }

        if (actor == Role.ADMIN && req.role != Role.STAFF) {
            throw new BadRequestException("Administrador só pode criar usuários do tipo STAFF.");
        }

        if (userRepository.existsByEmailAndActiveTrue(email)) {
            throw new ConflictException("Este e-mail já está em uso por um usuário ativo.");
        }

        userRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.isActive()) {
                userRepository.delete(existing);
                userRepository.flush();
            }
        });

        String temp = generateTempPassword(10);

        User saved = userRepository.save(User.builder()
                .name(req.name)
                .email(email)
                .role(req.role)
                .passwordHash(passwordEncoder.encode(temp))
                .active(true)
                .mustChangePassword(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        try {
            emailNotificationService.sendUserCreated(saved.getEmail(), saved.getName(), saved.getEmail(), temp);
        } catch (Exception e) {
            System.err.println("Falha ao enviar e-mail de boas-vindas: " + e.getMessage());
        }

        return wrapWithTemp(saved, temp);
    }

    public UserResponse update(Long id, UserUpdateRequest req) {
        Role actor = currentRole();
        User target = getUser(id);

        String email = normalizeEmail(req.email);
        if (email == null || email.isBlank()) {
            throw new BadRequestException("E-mail é obrigatório.");
        }

        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new BadRequestException("Este e-mail já está em uso.");
        }

        if (actor == Role.ADMIN && target.getRole() != Role.STAFF) {
            throw new BadRequestException("Administrador só pode editar usuários do tipo STAFF.");
        }

        if (actor == Role.ADMIN && req.role != Role.STAFF) {
            throw new BadRequestException("Administrador não pode alterar o perfil de usuário.");
        }

        String actorEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean updatedBySelf = actorEmail != null && actorEmail.equalsIgnoreCase(target.getEmail());

        target.setName(req.name);
        target.setEmail(email);
        target.setUpdatedAt(Instant.now());

        if (actor == Role.DEV) {
            target.setRole(req.role);
        }

        User saved = userRepository.save(target);

        if (updatedBySelf) {
            emailNotificationService.sendUserUpdatedBySelf(saved.getEmail(), saved.getName());
        } else {
            emailNotificationService.sendUserUpdatedByAdmin(saved.getEmail(), saved.getName(), actorEmail, actor.name());
        }

        return toResponse(saved);
    }

    public UserWithTempPasswordResponse resetPassword(Long id) {
        Role actor = currentRole();
        User target = getUser(id);

        if (actor == Role.ADMIN && target.getRole() != Role.STAFF) {
            throw new BadRequestException("Administrador só pode resetar senha de usuários do tipo STAFF.");
        }

        String temp = generateTempPassword(10);

        target.setPasswordHash(passwordEncoder.encode(temp));
        target.setMustChangePassword(true);
        target.setUpdatedAt(Instant.now());

        userRepository.save(target);

        // ✅ não pode quebrar
        emailNotificationService.sendUserCreated(target.getEmail(), target.getName(), target.getEmail(), temp);

        return wrapWithTemp(target, temp);
    }

    // ✅ HARD DELETE: apaga do banco de verdade
    public UserResponse deleteAndReturn(Long id) {
        Role actor = currentRole();
        User target = getUser(id);

        if (actor == Role.ADMIN && target.getRole() != Role.STAFF) {
            throw new BadRequestException("Administrador só pode excluir usuários do tipo STAFF.");
        }

        userRepository.delete(target);
        return toResponse(target);
    }

    public UserResponse activateAndReturn(Long id) {
        Role actor = currentRole();
        User target = getUser(id);

        if (actor == Role.ADMIN && target.getRole() != Role.STAFF) {
            throw new BadRequestException("Administrador só pode ativar usuários do tipo STAFF.");
        }

        target.setActive(true);
        target.setUpdatedAt(Instant.now());
        return toResponse(userRepository.save(target));
    }

    // ---------------- helpers ----------------

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
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

    private UserResponse toResponse(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.name = user.getName();
        r.email = user.getEmail();
        r.role = user.getRole();
        r.active = user.isActive();
        r.mustChangePassword = user.isMustChangePassword();
        r.createdAt = user.getCreatedAt();
        r.updatedAt = user.getUpdatedAt();
        return r;
    }

    private UserWithTempPasswordResponse wrapWithTemp(User user, String temp) {
        UserWithTempPasswordResponse resp = new UserWithTempPasswordResponse();
        resp.user = toResponse(user);
        resp.temporaryPassword = temp;
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
