package com.photoconnect.customer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Customer profile. Same database as everything else now (single Postgres),
 * but the {@code userId} foreign relationship to {@code users} is kept by
 * value (UUID) rather than a JPA association — preserves the module boundary.
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(length = 200)
    private String location;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_contact_method", nullable = false, length = 20)
    private ContactMethod preferredContactMethod = ContactMethod.EMAIL;

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
