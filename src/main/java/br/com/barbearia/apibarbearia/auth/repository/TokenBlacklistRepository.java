package br.com.barbearia.apibarbearia.auth.repository;


import br.com.barbearia.apibarbearia.auth.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByJti(String jti);
    long deleteByExpiresAtBefore(Instant now);
}
