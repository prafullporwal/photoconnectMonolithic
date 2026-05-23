package com.photoconnect.reviews.dto;

import java.util.UUID;

public record ReviewSummaryResponse(
        UUID photographerProfileId,
        double averageRating,
        long reviewCount
) {}
