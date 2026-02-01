package br.com.barbearia.apibarbearia.users.controller;

import br.com.barbearia.apibarbearia.users.dto.CompleteInviteRequest;
import br.com.barbearia.apibarbearia.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/public/invite")
@RequiredArgsConstructor
public class InviteController {

    private final UserService userService;

    @GetMapping("/{token}")
    public ResponseEntity<?> validateToken(@PathVariable String token) {
        var invite = userService.validateInviteToken(token);
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "email", invite.getEmail()
        ));
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeInvite(@Valid @RequestBody CompleteInviteRequest req) {
        var user = userService.completeInvite(req);
        return ResponseEntity.ok(Map.of(
                "message", "Conta criada com sucesso! Faça login.",
                "data", user
        ));
    }
}
