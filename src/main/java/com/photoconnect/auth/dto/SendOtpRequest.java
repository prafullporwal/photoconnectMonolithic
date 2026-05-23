package com.photoconnect.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendOtpRequest(
        @NotBlank
        @Pattern(regexp = "^\\+91[6-9]\\d{9}$",
                 message = "must be a valid Indian mobile in E.164 form, e.g. +919876543210")
        String phone
) {}
