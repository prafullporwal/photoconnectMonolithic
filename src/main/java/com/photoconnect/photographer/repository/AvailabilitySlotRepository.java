package com.photoconnect.photographer.repository;

import com.photoconnect.photographer.domain.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {

    List<AvailabilitySlot> findByPhotographerProfileIdOrderByAvailableDateAsc(UUID photographerProfileId);

    boolean existsByPhotographerProfileIdAndAvailableDate(UUID photographerProfileId, LocalDate date);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM AvailabilitySlot s WHERE s.id = :id " +
            "AND s.photographerProfileId = :photographerProfileId")
    int deleteByOwnedId(UUID photographerProfileId, UUID id);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM AvailabilitySlot s WHERE s.photographerProfileId = :photographerProfileId")
    int deleteAllForProfile(UUID photographerProfileId);
}
