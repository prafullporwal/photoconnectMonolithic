package com.photoconnect.photographer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Photographer's public profile.
 *
 * <p>{@code userId} links back to the {@code users} table — same Postgres
 * database now, but we keep the UUID-by-value relationship rather than a JPA
 * association to preserve the bounded-context boundary from the microservices
 * version.</p>
 */
@Entity
@Table(name = "photographer_profiles")
@Getter
@Setter
@NoArgsConstructor
public class PhotographerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(name = "years_of_experience", nullable = false)
    private int yearsOfExperience;

    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(nullable = false)
    private boolean available = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "photographer_specialties",
            joinColumns = @JoinColumn(name = "photographer_id"))
    @Column(name = "specialty", length = 100)
    private List<String> specialties = new ArrayList<>();

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
