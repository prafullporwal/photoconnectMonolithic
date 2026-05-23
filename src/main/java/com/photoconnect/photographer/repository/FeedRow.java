package com.photoconnect.photographer.repository;

import com.photoconnect.photographer.domain.MediaType;

import java.time.Instant;
import java.util.UUID;

/**
 * Flat JPQL projection for the marketplace feed query.
 */
public record FeedRow(
        UUID itemId,
        MediaType mediaType,
        String category,
        String mimeType,
        String publicUrl,
        Instant uploadedAt,
        UUID photographerProfileId,
        String photographerName,
        String photographerLocation
) {}
