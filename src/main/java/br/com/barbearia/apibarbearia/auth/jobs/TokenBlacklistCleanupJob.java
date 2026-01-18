package br.com.barbearia.apibarbearia.auth.jobs;

import br.com.barbearia.apibarbearia.auth.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class TokenBlacklistCleanupJob {

    private final TokenBlacklistRepository repository;

    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000L)
    public void cleanup() {
        repository.deleteByExpiresAtBefore(Instant.now());
    }
}
