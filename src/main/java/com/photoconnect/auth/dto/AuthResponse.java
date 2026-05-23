package com.photoconnect.auth.dto;

import com.photoconnect.auth.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        UUID userId,
        String email,
        Role role
) {}
