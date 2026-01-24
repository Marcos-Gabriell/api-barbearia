package br.com.barbearia.apibarbearia.users.controller;

import br.com.barbearia.apibarbearia.users.dtos.CompletePasswordResetRequest;
import br.com.barbearia.apibarbearia.users.dtos.ForgotPasswordRequest;
import br.com.barbearia.apibarbearia.users.dtos.ValidateCodeRequest;
import br.com.barbearia.apibarbearia.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/recovery")
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private final UserService userService;

    @PostMapping("/request")
    public ResponseEntity<?> requestRecovery(@RequestBody @Valid ForgotPasswordRequest req) {
        userService.requestPasswordReset(req);
        return ResponseEntity.ok(Map.of("message", "Se o e-mail existir, um código foi enviado."));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmRecovery(@RequestBody @Valid CompletePasswordResetRequest req) {
        userService.completePasswordReset(req);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso! Você já pode fazer login."));
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCode(@RequestBody @Valid ValidateCodeRequest req) {
        userService.validateRecoveryCode(req.getEmail(), req.getCode());
        return ResponseEntity.ok(Map.of("message", "Código válido."));
    }
}
