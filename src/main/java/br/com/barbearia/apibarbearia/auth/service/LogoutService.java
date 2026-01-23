package br.com.barbearia.apibarbearia.auth.service;

import br.com.barbearia.apibarbearia.auth.repository.RevokedTokenRepository;
import br.com.barbearia.apibarbearia.auth.security.JwtService;
import br.com.barbearia.apibarbearia.auth.security.RevokedToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class LogoutService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    public void logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String jwt = authHeader.substring(7); // Remove "Bearer "

        // Verifica se já não está na blacklist
        if (revokedTokenRepository.findByToken(jwt).isPresent()) {
            return;
        }

        // Pega a data de expiração do token para salvar no banco
        // (Assim você pode criar um job que apaga tokens do banco depois que expiram de verdade)
        var expiration = jwtService.getExpirationInstant(jwt);

        RevokedToken revokedToken = RevokedToken.builder()
                .token(jwt)
                .expirationDate(expiration)
                .build();

        revokedTokenRepository.save(revokedToken);
    }
}