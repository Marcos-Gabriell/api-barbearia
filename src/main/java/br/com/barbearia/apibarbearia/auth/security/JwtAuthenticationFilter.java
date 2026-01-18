package br.com.barbearia.apibarbearia.auth.security;

import br.com.barbearia.apibarbearia.auth.repository.TokenBlacklistRepository;
import br.com.barbearia.apibarbearia.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistRepository blacklistRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        // ✅ Só valida token aqui. NÃO envolve chain.doFilter em try/catch genérico.
        try {
            String jti = jwtService.getJti(token);

            if (jti != null && blacklistRepository.existsByJti(jti)) {
                unauthorized(response, "Sessão expirada. Faça login novamente.");
                return;
            }

            String email = jwtService.getSubject(token);

            if (email == null || email.isBlank()) {
                SecurityContextHolder.clearContext();
                unauthorized(response, "Token inválido ou expirado. Faça login novamente.");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userDetailsService.loadUserByUsername(email);

                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (RuntimeException ex) {
            // ✅ Aqui você pega apenas erros de token/parse/expiração que seu jwtService lançar.
            // (Se seu jwtService lança outras exceções, ajuste pra exceções específicas de JWT)
            SecurityContextHolder.clearContext();
            unauthorized(response, "Token inválido ou expirado. Faça login novamente.");
            return;
        }

        // ✅ IMPORTANTÍSSIMO: fora do try/catch
        chain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
