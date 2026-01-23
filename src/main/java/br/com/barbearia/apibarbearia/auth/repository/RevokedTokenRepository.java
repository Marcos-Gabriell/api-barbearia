package br.com.barbearia.apibarbearia.auth.repository;

import br.com.barbearia.apibarbearia.auth.security.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByToken(String token);
}