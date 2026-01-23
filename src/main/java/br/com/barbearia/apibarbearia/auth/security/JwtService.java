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
        if (user == null) {
            throw new IllegalArgumentException("User não pode ser nulo");
        }
        return buildToken(
                String.valueOf(user.getId()), // ID como Subject
                buildUserClaims(user),
                accessExpirationMs
        );
    }

    public String generateRefreshToken(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User não pode ser nulo");
        }
        return buildToken(
                String.valueOf(user.getId()), // ID como Subject
                buildUserClaims(user),
                refreshExpirationMs
        );
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
    // VALIDAÇÃO (CORRIGIDA)
    // ===================================================================================

    /**
     * Valida o token checando:
     * 1. Assinatura e Expiração
     * 2. Se pertence ao UserDetails informado
     * 3. Se o token é "fresco" (emitido DEPOIS da última invalidação/troca de senha)
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null || token.isBlank() || userDetails == null) {
            return false;
        }

        try {
            final String subject = getSubject(token);
            final String username = userDetails.getUsername(); // No seu caso, o username é o ID

            // 1. Verifica se o token pertence ao usuário
            boolean isUserValid = subject != null && subject.equals(username);

            // 2. Verifica se expirou
            boolean isNotExpired = !isTokenExpired(token);

            // 3. Verifica Invalidação (KICK)
            boolean isTokenFresh = true;

            // Aqui fazemos o cast seguro para acessar o campo tokenInvalidationTimestamp
            if (userDetails instanceof User) {
                User userEntity = (User) userDetails;
                if (userEntity.getTokenInvalidationTimestamp() != null) {
                    final Instant issuedAt = getIssuedAt(token);
                    // Se o token foi emitido ANTES da data de invalidação, ele é inválido.
                    if (issuedAt != null) {
                        isTokenFresh = issuedAt.isAfter(userEntity.getTokenInvalidationTimestamp());
                    }
                }
            }

            return isUserValid && isNotExpired && isTokenFresh;

        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token inválido na validação: {}", e.getMessage());
            return false;
        }
    }

    private Instant getIssuedAt(String token) {
        try {
            Date issuedAt = parseClaims(token).getIssuedAt();
            return issuedAt != null ? issuedAt.toInstant() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            Instant expiration = getExpirationInstant(token);
            return expiration != null && expiration.isBefore(Instant.now());
        } catch (Exception e) {
            return true;
        }
    }

    // ===================================================================================
    // EXTRAÇÃO E PARSING
    // ===================================================================================

    public Claims parseClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token não pode ser nulo ou vazio");
        }

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token expirado: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.debug("Token inválido: {}", e.getMessage());
            throw new IllegalArgumentException("Token JWT inválido", e);
        }
    }

    public String getSubject(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserId(String token) {
        try {
            String subject = getSubject(token);
            return subject != null ? Long.parseLong(subject) : null;
        } catch (NumberFormatException e) {
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
        try {
            Claims claims = parseClaims(token);
            Date exp = claims.getExpiration();
            return exp != null ? exp.toInstant() : null;
        } catch (ExpiredJwtException e) {
            Date exp = e.getClaims().getExpiration();
            return exp != null ? exp.toInstant() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String getJti(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getId();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getId();
        } catch (Exception e) {
            return null;
        }
    }

    // Helper genérico para pegar claims
    private <T> T getClaim(String token, String claimName, Class<T> type) {
        try {
            Claims claims = parseClaims(token);
            return claims.get(claimName, type);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get(claimName, type);
        }
    }

    // ===================================================================================
    // MÉTODOS ADICIONAIS
    // ===================================================================================

    public boolean isTokenExpiredOrInvalid(String token) {
        try {
            parseClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public Map<String, Object> getAllClaims(String token) {
        try {
            Claims claims = parseClaims(token);
            return new HashMap<>(claims);
        } catch (ExpiredJwtException e) {
            return new HashMap<>(e.getClaims());
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}