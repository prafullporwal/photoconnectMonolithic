package com.photoconnect.customer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "inquiries")
@Getter
@Setter
@NoArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** auth-module userId of the customer who created this inquiry. */
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    /** PhotographerProfile PK. */
    @Column(name = "photographer_profile_id", nullable = false)
    private UUID photographerProfileId;

    /** auth-module userId of the photographer — denormalised for inbox queries. */
    @Column(name = "photographer_user_id", nullable = false)
    private UUID photographerUserId;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(length = 200)
    private String location;

    @Column(precision = 10, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.NEW;

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
        if (status == null) status = InquiryStatus.NEW;
    }

    @PreUpdate
    void onPreUpdate() {
        updatedAt = Instant.now();
    }
}
