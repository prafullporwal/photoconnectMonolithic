package com.photoconnect.reviews.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "photographer_profile_id", nullable = false)
    private UUID photographerProfileId;

    @Column(name = "photographer_user_id", nullable = false)
    private UUID photographerUserId;

    /** The completed inquiry that authorised this review. */
    @Column(name = "inquiry_id", nullable = false)
    private UUID inquiryId;

    @Column(nullable = false)
    private short rating;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @PrePersist
    void onPrePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = Instant.now();
    }
}
