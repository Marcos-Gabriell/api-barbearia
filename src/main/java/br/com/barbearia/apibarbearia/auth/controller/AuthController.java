package br.com.barbearia.apibarbearia.auth.controller;

import br.com.barbearia.apibarbearia.auth.dto.*;
import br.com.barbearia.apibarbearia.auth.entity.TokenBlacklist;
import br.com.barbearia.apibarbearia.auth.repository.TokenBlacklistRepository;
import br.com.barbearia.apibarbearia.auth.security.JwtService;
import br.com.barbearia.apibarbearia.auth.security.LoginRateLimiter;
import br.com.barbearia.apibarbearia.common.exception.UnauthorizedException;
import br.com.barbearia.apibarbearia.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.Instant;
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

            var user = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado."));

            Map<String, Object> claims = Map.of(
                    "role", user.getRole().name(),
                    "userId", user.getId(),
                    "active", user.isActive()
            );

            String accessToken = jwtService.generateAccessToken(user.getEmail(), claims);
            String refreshToken = jwtService.generateRefreshToken(user.getEmail(), claims);

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

        String jti = jwtService.getJti(refreshToken);
        if (blacklistRepository.existsByJti(jti)) {
            throw new UnauthorizedException("Sessão expirada. Faça login novamente.");
        }

        String email = jwtService.getSubject(refreshToken);

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuário desativado.");
        }

        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "userId", user.getId(),
                "active", user.isActive()
        );

        String newAccess = jwtService.generateAccessToken(user.getEmail(), claims);
        String newRefresh = jwtService.generateRefreshToken(user.getEmail(), claims);

        RefreshResponse resp = new RefreshResponse();
        resp.token = newAccess;
        resp.refreshToken = newRefresh;

        return Map.of(
                "message", "Token atualizado com sucesso.",
                "data", resp
        );
    }

    @PostMapping("/logout")
    public Object logout(@Valid @RequestBody LogoutRequest req) {
        String token = req.token;

        String jti = jwtService.getJti(token);
        Instant exp = jwtService.getExpirationInstant(token);

        if (!blacklistRepository.existsByJti(jti)) {
            blacklistRepository.save(TokenBlacklist.builder()
                    .jti(jti)
                    .expiresAt(exp)
                    .createdAt(Instant.now())
                    .build());
        }

        return Map.of("message", "Logout realizado com sucesso.");
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        String email = authentication.getName();

        var user = userRepository.findByEmail(email)
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
}
