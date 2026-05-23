package com.photoconnect.auth.dto;

import com.photoconnect.auth.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotNull Role role
) {}
