package com.photoconnect.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "auth.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank String audience,
        @NotNull  Duration accessTokenTtl,
        @NotNull  Duration refreshTokenTtl,
        @NotBlank String privateKeyPath,
        @NotBlank String publicKeyPath
) {}
