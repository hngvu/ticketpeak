package io.qzz.hoangvu.ticketpeak.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Service
public class RefreshTokenStore {

    private final StringRedisTemplate redisTemplate;
    private final long refreshTokenExpirySeconds;

    public RefreshTokenStore(
            StringRedisTemplate redisTemplate,
            @Value("${app.jwt.refresh-token-expiry:604800}") long refreshTokenExpirySeconds
    ) {
        this.redisTemplate = redisTemplate;
        this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;
    }

    public void store(UUID accountId, String refreshTokenId) {
        redisTemplate.opsForValue().set(key(accountId), refreshTokenId, Duration.ofSeconds(refreshTokenExpirySeconds));
    }

    public boolean matches(UUID accountId, String refreshTokenId) {
        String currentTokenId = redisTemplate.opsForValue().get(key(accountId));
        return Objects.equals(currentTokenId, refreshTokenId);
    }

    public void delete(UUID accountId) {
        redisTemplate.delete(key(accountId));
    }

    private String key(UUID accountId) {
        return "auth:refresh:" + accountId;
    }
}
