package br.com.barbearia.apibarbearia.auth.controller;

import br.com.barbearia.apibarbearia.auth.dto.*;
import br.com.barbearia.apibarbearia.auth.repository.TokenBlacklistRepository;
import br.com.barbearia.apibarbearia.auth.security.JwtService;
import br.com.barbearia.apibarbearia.auth.security.LoginRateLimiter;
import br.com.barbearia.apibarbearia.auth.service.LogoutService;
import br.com.barbearia.apibarbearia.common.exception.UnauthorizedException;
import br.com.barbearia.apibarbearia.users.entity.User;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final LoginRateLimiter rateLimiter;
    private final TokenBlacklistRepository blacklistRepository;
    private final LogoutService logoutService;

    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        String email = req.email == null ? "" : req.email.toLowerCase().trim();
        String ip = request.getRemoteAddr();
        String key = ip + "|" + email;

        rateLimiter.check(key);

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, req.password)
            );

            rateLimiter.onSuccess(key);

            // Busca usuário pelo email (autenticação bem-sucedida)
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado."));

            // ✅ AGORA: Gera tokens usando o objeto User (JwtService usa ID como subject)
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            RefreshResponse resp = new RefreshResponse();
            resp.token = accessToken;
            resp.refreshToken = refreshToken;

            return Map.of(
                    "message", "Login realizado com sucesso.",
                    "data", resp
            );

        } catch (BadCredentialsException ex) {
            rateLimiter.onFail(key);
            throw new UnauthorizedException("E-mail ou senha inválidos.");
        } catch (DisabledException ex) {
            throw new UnauthorizedException("Usuário desativado.");
        }
    }

    @PostMapping("/refresh")
    public Object refresh(@Valid @RequestBody RefreshRequest req) {
        String refreshToken = req.refreshToken;

        // Verifica se token está na blacklist
        String jti = jwtService.getJti(refreshToken);
        if (blacklistRepository.existsByJti(jti)) {
            throw new UnauthorizedException("Sessão expirada. Faça login novamente.");
        }

        // ✅ AGORA: O subject do token é o ID (String)
        String userIdStr = jwtService.getSubject(refreshToken);
        if (userIdStr == null) {
            throw new UnauthorizedException("Token inválido.");
        }

        // Converte ID String para Long
        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("ID de usuário inválido no token.");
        }

        // Busca usuário pelo ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário desativado.");
        }

        // ✅ Gera novos tokens usando o objeto User
        String newAccess = jwtService.generateAccessToken(user);
        String newRefresh = jwtService.generateRefreshToken(user);

        RefreshResponse resp = new RefreshResponse();
        resp.token = newAccess;
        resp.refreshToken = newRefresh;

        return Map.of(
                "message", "Token atualizado com sucesso.",
                "data", resp
        );
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        // ✅ AGORA: authentication.getName() retorna o ID (String)
        String userIdStr = authentication.getName();

        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("ID de usuário inválido na sessão.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado."));

        return Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole().name(),
                "active", user.isActive(),
                "mustChangePassword", user.isMustChangePassword()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        logoutService.logout(request);
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso no backend."));
    }
}