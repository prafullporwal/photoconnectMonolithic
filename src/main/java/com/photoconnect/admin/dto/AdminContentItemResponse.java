package com.photoconnect.admin.dto;

import com.photoconnect.photographer.domain.MediaType;

import java.time.Instant;
import java.util.UUID;

public record AdminContentItemResponse(
        UUID id,
        UUID photographerProfileId,
        String photographerDisplayName,
        MediaType mediaType,
        String category,
        String mimeType,
        long sizeBytes,
        String publicUrl,
        String originalFileName,
        Instant uploadedAt
) {}
