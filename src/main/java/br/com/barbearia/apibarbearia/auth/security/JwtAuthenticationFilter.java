package br.com.barbearia.apibarbearia.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Se já tiver auth no contexto, segue normal
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // Sem Bearer -> deixa passar (rotas públicas continuam funcionando)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7).trim();

        // Bearer vazio
        if (jwt.isEmpty()) {
            writeUnauthorized(response, request, "Token ausente.");
            return;
        }

        // Extrai o subject (ID)
        final String subject = jwtService.getSubject(jwt);

        // Se não extrair subject, token provavelmente inválido
        if (subject == null || subject.isBlank()) {
            writeUnauthorized(response, request, "Token inválido ou malformado.");
            return;
        }

        try {
            // Carrega user pelo "username" (no seu caso: ID em String)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(subject);

            // Valida token com o user do banco
            if (!jwtService.isTokenValid(jwt, userDetails)) {
                writeUnauthorized(response, request, "Token expirado ou inválido.");
                return;
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            filterChain.doFilter(request, response);

        } catch (UsernameNotFoundException ex) {
            writeUnauthorized(response, request, "Usuário do token não encontrado.");
        } catch (Exception ex) {
            // Qualquer exceção inesperada no filtro não deve virar 500 “mudo”
            log.debug("Falha na autenticação JWT: {}", ex.getMessage());
            writeUnauthorized(response, request, "Falha ao validar autenticação.");
        }
    }

    /**
     * Retorna 401 com JSON padrão.
     * Isso te ajuda a debugar no Postman e padroniza os erros.
     */
    private void writeUnauthorized(HttpServletResponse response, HttpServletRequest request, String message) throws IOException {
        if (response.isCommitted()) return;

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", message);
        body.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
