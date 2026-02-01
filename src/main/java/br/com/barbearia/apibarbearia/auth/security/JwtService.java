package br.com.barbearia.apibarbearia.auth.security;

import br.com.barbearia.apibarbearia.users.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private final Key key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expirationMinutes}") long expirationMinutes,
            @Value("${app.jwt.refreshExpirationMinutes}") long refreshExpirationMinutes
    ) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret não pode ser nulo ou vazio");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = expirationMinutes * 60_000;
        this.refreshExpirationMs = refreshExpirationMinutes * 60_000;
    }

    // ===================================================================================
    // GERAÇÃO DE TOKENS
    // ===================================================================================

    public String generateAccessToken(User user) {
        if (user == null) throw new IllegalArgumentException("User não pode ser nulo");
        return buildToken(String.valueOf(user.getId()), buildUserClaims(user), accessExpirationMs);
    }

    public String generateRefreshToken(User user) {
        if (user == null) throw new IllegalArgumentException("User não pode ser nulo");
        return buildToken(String.valueOf(user.getId()), buildUserClaims(user), refreshExpirationMs);
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject não pode ser nulo ou vazio");
        }
        return buildToken(subject, claims, accessExpirationMs);
    }

    public String generateRefreshToken(String subject, Map<String, Object> claims) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException("Subject não pode ser nulo ou vazio");
        }
        return buildToken(subject, claims, refreshExpirationMs);
    }

    private Map<String, Object> buildUserClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().name());
        claims.put("name", user.getName());
        claims.put("active", user.isActive());
        claims.put("mustChangePassword", user.isMustChangePassword());
        return claims;
    }

    private String buildToken(String subject, Map<String, Object> claims, long ttlMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMs);

        JwtBuilder builder = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256);

        if (claims != null && !claims.isEmpty()) {
            builder.addClaims(claims);
        }

        return builder.compact();
    }

    // ===================================================================================
    // VALIDAÇÃO (CORRIGIDA + TRATAMENTO)
    // ===================================================================================

    /**
     * Valida o token checando:
     * 1) Assinatura e Expiração
     * 2) Se pertence ao usuário (subject = ID do User)
     * 3) Se o token é "fresco" (emitido depois da invalidação)
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null || token.isBlank() || userDetails == null) return false;

        try {
            // 1) parse + assinatura + expiração
            Claims claims = parseClaims(token); // aqui já valida assinatura/expiração
            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) return false;

            // 2) confere se o token pertence ao usuário
            boolean isUserValid = false;

            if (userDetails instanceof User) {
                User u = (User) userDetails;

                // subject é ID (string numérica)
                try {
                    Long tokenUserId = Long.parseLong(subject);
                    isUserValid = tokenUserId.equals(u.getId());
                } catch (NumberFormatException ex) {
                    // se por algum motivo não for numérico, tenta por email (fallback)
                    isUserValid = subject.equalsIgnoreCase(u.getEmail());
                }

                // 3) confere invalidação (kick)
                if (u.getTokenInvalidationTimestamp() != null) {
                    Date issuedAt = claims.getIssuedAt();
                    if (issuedAt != null) {
                        Instant issuedInstant = issuedAt.toInstant();
                        if (!issuedInstant.isAfter(u.getTokenInvalidationTimestamp())) {
                            return false;
                        }
                    }
                }

            } else {
                // fallback: compara subject com username
                isUserValid = subject.equals(userDetails.getUsername());
            }

            return isUserValid;

        } catch (ExpiredJwtException e) {
            // token expirado -> inválido (não lança 500)
            log.debug("Token expirado: {}", e.getMessage());
            return false;

        } catch (JwtException | IllegalArgumentException e) {
            // token inválido / assinatura errada / malformado
            log.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    // ===================================================================================
    // EXTRAÇÃO E PARSING
    // ===================================================================================

    /**
     * Parse que valida assinatura e expiração.
     * Se expirado lança ExpiredJwtException.
     */
    public Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token não pode ser nulo ou vazio");
        }

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getSubject(String token) {
        if (token == null || token.isBlank()) return null;

        try {
            return parseClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            // se quiser ler subject mesmo expirado
            return e.getClaims() != null ? e.getClaims().getSubject() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserId(String token) {
        try {
            String subject = getSubject(token);
            if (subject == null) return null;
            return Long.parseLong(subject);
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserEmail(String token) {
        try {
            return getClaim(token, "email", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String getUserRole(String token) {
        try {
            return getClaim(token, "role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Instant getExpirationInstant(String token) {
        if (token == null || token.isBlank()) return null;

        try {
            Date exp = parseClaims(token).getExpiration();
            return exp != null ? exp.toInstant() : null;
        } catch (ExpiredJwtException e) {
            Date exp = e.getClaims() != null ? e.getClaims().getExpiration() : null;
            return exp != null ? exp.toInstant() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getJti(String token) {
        if (token == null || token.isBlank()) return null;

        try {
            return parseClaims(token).getId();
        } catch (ExpiredJwtException e) {
            return e.getClaims() != null ? e.getClaims().getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Helper genérico para pegar claims
    private <T> T getClaim(String token, String claimName, Class<T> type) {
        if (token == null || token.isBlank()) return null;

        try {
            Claims claims = parseClaims(token);
            return claims.get(claimName, type);
        } catch (ExpiredJwtException e) {
            return e.getClaims() != null ? e.getClaims().get(claimName, type) : null;
        }
    }

    // ===================================================================================
    // MÉTODOS ADICIONAIS
    // ===================================================================================

    public boolean isTokenExpiredOrInvalid(String token) {
        if (token == null || token.isBlank()) return true;
        try {
            parseClaims(token);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    public Map<String, Object> getAllClaims(String token) {
        Map<String, Object> out = new HashMap<>();
        if (token == null || token.isBlank()) return out;

        try {
            Claims claims = parseClaims(token);
            out.putAll(claims);
            return out;
        } catch (ExpiredJwtException e) {
            if (e.getClaims() != null) out.putAll(e.getClaims());
            return out;
        } catch (Exception e) {
            return out;
        }
    }
}
