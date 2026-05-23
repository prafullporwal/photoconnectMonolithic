package com.photoconnect.photographer.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "availability_slots")
@Getter
@Setter
@NoArgsConstructor
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "photographer_profile_id", nullable = false)
    private UUID photographerProfileId;

    @Column(name = "available_date", nullable = false)
    private LocalDate availableDate;

    @Column(length = 200)
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onPrePersist() {
        createdAt = Instant.now();
    }
}
