package com.photoconnect.photographer.dto;

import com.photoconnect.photographer.domain.MediaType;

import java.time.Instant;
import java.util.UUID;

public record PortfolioItemResponse(
        UUID id,
        UUID photographerProfileId,
        MediaType mediaType,
        String category,
        String mimeType,
        long sizeBytes,
        String publicUrl,
        int displayOrder,
        Instant uploadedAt,
        String originalFileName
) {}
