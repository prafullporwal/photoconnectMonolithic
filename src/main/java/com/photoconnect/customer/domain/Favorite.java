package com.photoconnect.customer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Customer bookmark — points at a specific portfolio item (not a photographer).
 */
@Entity
@Table(name = "favorites")
@Getter
@Setter
@NoArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "portfolio_item_id", nullable = false)
    private UUID portfolioItemId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onPrePersist() {
        createdAt = Instant.now();
    }
}
