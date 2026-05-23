package com.photoconnect.auth.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.otp")
public record OtpProperties(
        @NotNull Provider provider,
        boolean devMode,
        @Min(4) int length,
        @NotNull Duration ttl,
        @NotNull Duration resendCooldown,
        @Min(1) int maxAttempts
) {
    public enum Provider { DEV }
}
