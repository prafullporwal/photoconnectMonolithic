package com.photoconnect.photographer.dto;

import com.photoconnect.photographer.domain.MediaType;

import java.time.Instant;
import java.util.UUID;

public record FeedItemResponse(
        UUID id,
        MediaType mediaType,
        String category,
        String mimeType,
        String publicUrl,
        Instant uploadedAt,
        PhotographerSnippet photographer
) {
    public record PhotographerSnippet(UUID id, String displayName, String location) {}
}
