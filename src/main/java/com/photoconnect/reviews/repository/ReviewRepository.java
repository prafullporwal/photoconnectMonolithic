package com.photoconnect.reviews.repository;

import com.photoconnect.reviews.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByPhotographerProfileIdOrderByCreatedAtDesc(UUID photographerProfileId);

    List<Review> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    boolean existsByCustomerIdAndPhotographerProfileId(UUID customerId, UUID photographerProfileId);

    @Query("""
            select avg(cast(r.rating as double)) as averageRating,
                   count(r)                       as reviewCount
            from   Review r
            where  r.photographerProfileId = :profileId
            """)
    ReviewSummary summarise(@Param("profileId") UUID photographerProfileId);
}
