package br.com.barbearia.apibarbearia.auth.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        // Extrai o ID do usuário (subject) e o e-mail
        final String userId = jwtService.getSubject(jwt);

        // Opcional: extrair email se sua lógica de load depender disso,
        // mas como seu UserDetails usa ID, focamos no userId.
        // final String userEmail = jwtService.getUserEmail(jwt);

        // Se não conseguiu extrair o ID, segue sem autenticar
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Se já está autenticado no contexto, segue
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Carrega o UserDetails usando o ID (Subject do token)
            // Aqui é importante que seu UserDetailsService busque pelo ID se receber um número string
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userId);

            // Valida o token passando o UserDetails recuperado do banco
            // O JwtService fará o cast para User entity e checará o timestamp
            if (jwtService.isTokenValid(jwt, userDetails)) {

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Autentica o usuário no contexto do Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Log discreto para não poluir em caso de token expirado comum,
            // mas útil para debug se for erro de banco/lógica
            log.debug("Falha na autenticação JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}