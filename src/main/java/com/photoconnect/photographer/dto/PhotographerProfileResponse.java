package com.photoconnect.photographer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PhotographerProfileResponse(
        UUID id,
        UUID userId,
        String displayName,
        String bio,
        String location,
        int yearsOfExperience,
        BigDecimal pricePerHour,
        boolean available,
        List<String> specialties,
        Instant createdAt,
        Instant updatedAt
) {}
