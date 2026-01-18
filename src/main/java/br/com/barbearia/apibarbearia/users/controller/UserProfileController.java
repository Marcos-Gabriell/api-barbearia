package br.com.barbearia.apibarbearia.users.controller;

import br.com.barbearia.apibarbearia.common.exception.BadRequestException;
import br.com.barbearia.apibarbearia.common.exception.NotFoundException;
import br.com.barbearia.apibarbearia.users.dtos.UpdateMyProfileRequest;
import br.com.barbearia.apibarbearia.users.dtos.UserResponse;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;

    @PutMapping
    public ResponseEntity<?> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequest req,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        if (userRepository.existsByEmailAndIdNot(req.email.toLowerCase(), user.getId())) {
            throw new BadRequestException("Este e-mail já está em uso.");
        }

        user.setName(req.name);
        user.setEmail(req.email.toLowerCase());
        user.setUpdatedAt(Instant.now());

        UserResponse resp = toResponse(userRepository.save(user));

        return ResponseEntity.ok(Map.of(
                "message", "Dados atualizados com sucesso.",
                "data", resp
        ));
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
}
