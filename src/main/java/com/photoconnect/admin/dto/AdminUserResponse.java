package com.photoconnect.admin.dto;

import com.photoconnect.auth.domain.Role;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(
        UUID id,
        String email,
        String phone,
        Role role,
        boolean enabled,
        Instant createdAt
) {}
