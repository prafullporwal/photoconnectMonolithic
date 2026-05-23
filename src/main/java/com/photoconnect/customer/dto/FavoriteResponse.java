package com.photoconnect.customer.dto;

import com.photoconnect.photographer.dto.FeedItemResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * Response shape for a single saved content item.
 *
 * <p>Reuses {@link FeedItemResponse} directly — no more tolerant-reader DTOs
 * needed because there's no HTTP boundary between the customer and photographer
 * modules.</p>
 */
public record FavoriteResponse(
        UUID id,
        UUID portfolioItemId,
        FeedItemResponse item,   // nullable if the item was deleted
        Instant createdAt
) {
    public static FavoriteResponse withoutItem(UUID id, UUID portfolioItemId, Instant createdAt) {
        return new FavoriteResponse(id, portfolioItemId, null, createdAt);
    }
}
