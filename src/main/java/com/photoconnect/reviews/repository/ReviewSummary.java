package com.photoconnect.reviews.repository;

/**
 * Interface projection for the aggregate query (avg rating + count).
 *
 * <p>{@code averageRating} is boxed because {@code AVG} returns {@code NULL}
 * when there are no rows.</p>
 */
public interface ReviewSummary {
    Double getAverageRating();
    long getReviewCount();
}
