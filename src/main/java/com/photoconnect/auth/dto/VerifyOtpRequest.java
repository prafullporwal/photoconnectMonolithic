package com.photoconnect.auth.dto;

import com.photoconnect.auth.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
        @NotBlank
        @Pattern(regexp = "^\\+91[6-9]\\d{9}$") String phone,
        @NotBlank @Size(min = 4, max = 10) String code,
        @NotNull Role role,
        @Email @Size(max = 255) String email
) {}
