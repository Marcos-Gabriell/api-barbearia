package br.com.barbearia.apibarbearia.users.controller;

import br.com.barbearia.apibarbearia.auth.security.JwtService;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.users.dto.*;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import br.com.barbearia.apibarbearia.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

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

        return ResponseEntity.ok(Map.of("data", r));
    }

    @PutMapping
    public ResponseEntity<?> updateMyProfile(@Valid @RequestBody UpdateMyProfileRequest req,
                                             Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        UserResponse updated = userService.updateMyProfile(user, req);

        String message = "Dados atualizados com sucesso.";
        if (updated.getPendingEmail() != null && !updated.getPendingEmail().isBlank()) {
            message = "Dados salvos. Um código de verificação foi enviado para o novo e-mail.";
        }

        return ResponseEntity.ok(Map.of(
                "message", message,
                "data", updated
        ));
    }

    @PostMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestBody @Valid ConfirmEmailRequest req,
                                          Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        User updatedUser = userService.confirmEmailUpdate(user, req.getCode());

        String accessToken = jwtService.generateAccessToken(updatedUser);
        String refreshToken = jwtService.generateRefreshToken(updatedUser);

        return ResponseEntity.ok(Map.of(
                "message", "E-mail confirmado e atualizado com sucesso!",
                "data", Map.of(
                        "token", accessToken,
                        "refreshToken", refreshToken
                )
        ));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest req,
                                            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        userService.changePassword(user, req);
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso."));
    }
}
