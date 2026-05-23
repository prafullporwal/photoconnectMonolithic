package com.photoconnect.photographer.repository;

import com.photoconnect.photographer.domain.MediaType;
import com.photoconnect.photographer.domain.PortfolioItem;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, UUID> {

    List<PortfolioItem> findByPhotographerProfileIdOrderByDisplayOrderAscUploadedAtAsc(UUID profileId);

    List<PortfolioItem> findByPhotographerProfileIdAndMediaTypeOrderByDisplayOrderAscUploadedAtAsc(
            UUID profileId, MediaType mediaType);

    List<PortfolioItem> findByPhotographerProfileIdAndCategoryOrderByDisplayOrderAscUploadedAtAsc(
            UUID profileId, String category);

    @Query("""
        SELECT new com.photoconnect.photographer.repository.FeedRow(
            pi.id, pi.mediaType, pi.category, pi.mimeType, pi.publicUrl, pi.uploadedAt,
            pp.id, pp.displayName, pp.location)
        FROM PortfolioItem pi
        JOIN com.photoconnect.photographer.domain.PhotographerProfile pp
          ON pp.id = pi.photographerProfileId
        WHERE pp.available = true
        ORDER BY pi.uploadedAt DESC
        """)
    List<FeedRow> findFeed(Limit limit);

    @Query("""
        SELECT new com.photoconnect.photographer.repository.FeedRow(
            pi.id, pi.mediaType, pi.category, pi.mimeType, pi.publicUrl, pi.uploadedAt,
            pp.id, pp.displayName, pp.location)
        FROM PortfolioItem pi
        JOIN com.photoconnect.photographer.domain.PhotographerProfile pp
          ON pp.id = pi.photographerProfileId
        WHERE pi.id = :itemId
        """)
    Optional<FeedRow> findFeedById(UUID itemId);
}
