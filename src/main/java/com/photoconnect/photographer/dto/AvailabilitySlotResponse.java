package com.photoconnect.photographer.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AvailabilitySlotResponse(
        UUID id,
        UUID photographerProfileId,
        LocalDate availableDate,
        String note,
        Instant createdAt
) {}
