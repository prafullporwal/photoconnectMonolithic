package com.photoconnect.auth.service;

import com.photoconnect.auth.config.OtpProperties;
import com.photoconnect.auth.exception.AuthExceptions.InvalidOtpException;
import com.photoconnect.auth.exception.AuthExceptions.OtpCooldownException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

/**
 * OTP minting, storage and verification, all in Redis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final String K_CODE     = "auth:otp:code:";
    private static final String K_ATTEMPTS = "auth:otp:attempts:";
    private static final String K_COOLDOWN = "auth:otp:cooldown:";

    private final StringRedisTemplate redis;
    private final OtpProperties props;
    private final OtpDeliveryService delivery;
    private final SecureRandom random = new SecureRandom();

    public Issued sendOtp(String phone) {
        Boolean acquired = redis.opsForValue().setIfAbsent(
                K_COOLDOWN + phone, "1", props.resendCooldown());
        if (Boolean.FALSE.equals(acquired)) {
            Long remaining = redis.getExpire(K_COOLDOWN + phone);
            throw new OtpCooldownException(remaining == null ? 0 : remaining);
        }

        String code = generateCode();
        Duration ttl = props.ttl();
        redis.opsForValue().set(K_CODE + phone, code, ttl);
        redis.opsForValue().set(K_ATTEMPTS + phone, Integer.toString(props.maxAttempts()), ttl);

        try {
            delivery.deliver(phone, code);
        } catch (RuntimeException ex) {
            redis.delete(K_CODE + phone);
            redis.delete(K_ATTEMPTS + phone);
            redis.delete(K_COOLDOWN + phone);
            throw ex;
        }

        return new Issued(code, Instant.now().plus(ttl));
    }

    public boolean verify(String phone, String submitted) {
        String stored = redis.opsForValue().get(K_CODE + phone);
        if (stored == null) {
            throw new InvalidOtpException("no active code for this phone");
        }

        if (!constantTimeEquals(stored, submitted)) {
            Long left = redis.opsForValue().decrement(K_ATTEMPTS + phone);
            if (left == null || left <= 0) {
                redis.delete(K_CODE + phone);
                redis.delete(K_ATTEMPTS + phone);
                throw new InvalidOtpException("too many wrong attempts");
            }
            throw new InvalidOtpException("incorrect code; " + left + " attempt(s) left");
        }

        redis.delete(K_CODE + phone);
        redis.delete(K_ATTEMPTS + phone);
        log.info("OTP verified for phone={}", phone);
        return true;
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, props.length());
        int n = random.nextInt(bound);
        return String.format("%0" + props.length() + "d", n);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);
        }
        return diff == 0;
    }

    public record Issued(String code, Instant expiresAt) {}
}
