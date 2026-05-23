package com.photoconnect.auth.dto;

import java.time.Instant;

public record SendOtpResponse(
        String phone,
        Instant expiresAt,
        String devCode
) {}
