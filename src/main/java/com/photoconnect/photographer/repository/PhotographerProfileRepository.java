package com.photoconnect.photographer.repository;

import com.photoconnect.photographer.domain.PhotographerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PhotographerProfileRepository extends JpaRepository<PhotographerProfile, UUID> {

    Optional<PhotographerProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<PhotographerProfile> findAllByAvailableTrue();
}
