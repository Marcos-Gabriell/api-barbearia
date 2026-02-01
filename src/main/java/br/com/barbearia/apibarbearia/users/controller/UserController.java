package br.com.barbearia.apibarbearia.users.controller;

import br.com.barbearia.apibarbearia.users.dto.InviteUserRequest;
import br.com.barbearia.apibarbearia.users.dto.UserResponse;
import br.com.barbearia.apibarbearia.users.dto.UserUpdateRequest;
import br.com.barbearia.apibarbearia.users.dto.UserWithTempPasswordResponse;
import br.com.barbearia.apibarbearia.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('DEV','ADMIN')")
public class UserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<?> list() {
        List<UserResponse> users = service.list();
        return ResponseEntity.ok(Map.of(
                "message", "Usuários listados com sucesso.",
                "data", users
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        UserResponse user = service.getById(id);
        return ResponseEntity.ok(Map.of(
                "message", "Usuário encontrado com sucesso.",
                "data", user
        ));
    }

    // ✅ REMOVIDO: create manual (POST /api/users)

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
        UserResponse updated = service.update(id, req);
        return ResponseEntity.ok(Map.of(
                "message", "Usuário atualizado com sucesso.",
                "data", updated
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        UserResponse user = service.deleteAndReturn(id);
        return ResponseEntity.ok(Map.of(
                "message", "Usuário excluído com sucesso.",
                "data", user
        ));
    }

    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        UserWithTempPasswordResponse resp = service.resetPassword(id);
        return ResponseEntity.ok(Map.of(
                "message", "Senha resetada com sucesso.",
                "data", resp
        ));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        UserResponse user = service.activateAndReturn(id);
        return ResponseEntity.ok(Map.of(
                "message", "Usuário ativado com sucesso.",
                "data", user
        ));
    }

    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole('DEV','ADMIN')")
    public ResponseEntity<?> sendInvite(@Valid @RequestBody InviteUserRequest req) {
        service.inviteUser(req);
        return ResponseEntity.ok(Map.of(
                "message", "Convite enviado com sucesso para " + req.getEmail()
        ));
    }
}
