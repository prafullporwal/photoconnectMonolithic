package com.photoconnect.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Redis-backed blacklist for revoked access-token {@code jti}s.
 *
 * <p>Keys are written with TTL equal to the token's remaining life, so Redis
 * auto-evicts them when the token would have expired anyway.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redis;

    public void blacklist(String jti, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (ttl.isNegative() || ttl.isZero()) return;
        try {
            redis.opsForValue().set(KEY_PREFIX + jti, "1", ttl);
        } catch (Exception ex) {
            log.warn("Redis unavailable for blacklist write of jti={}: {}", jti, ex.getMessage());
        }
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null) return false;
        try {
            return Boolean.TRUE.equals(redis.hasKey(KEY_PREFIX + jti));
        } catch (Exception ex) {
            log.warn("Redis unavailable for blacklist check of jti={}: {}", jti, ex.getMessage());
            return false;
        }
    }
}
