package com.photoconnect.photographer.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record UpdateProfileRequest(

        @NotBlank(message = "Display name is required")
        @Size(max = 100)
        String displayName,

        @Size(max = 2000)
        String bio,

        @NotBlank(message = "Location is required")
        @Size(max = 200)
        String location,

        @NotNull
        @Min(0) @Max(60)
        Integer yearsOfExperience,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal pricePerHour,

        @NotNull
        @Size(max = 20)
        List<@NotBlank @Size(max = 100) String> specialties,

        boolean available
) {}
