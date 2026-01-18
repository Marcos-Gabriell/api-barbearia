package br.com.barbearia.apibarbearia.auth.security;

import br.com.barbearia.apibarbearia.common.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    private static class Entry {
        int attempts;
        Instant blockedUntil;
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public void check(String key) {
        Entry e = store.get(key);
        if (e == null || e.blockedUntil == null) return;

        if (Instant.now().isBefore(e.blockedUntil)) {
            throw new UnauthorizedException("Muitas tentativas. Aguarde alguns minutos e tente novamente.");
        }
    }

    public void onFail(String key) {
        Entry e = store.computeIfAbsent(key, k -> new Entry());
        e.attempts++;

        if (e.attempts >= 10) {
            e.attempts = 0;
            e.blockedUntil = Instant.now().plusSeconds(5 * 60);
        }
    }

    public void onSuccess(String key) {
        store.remove(key);
    }
}
