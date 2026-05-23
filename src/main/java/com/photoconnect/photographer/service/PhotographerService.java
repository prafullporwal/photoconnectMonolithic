package com.photoconnect.photographer.service;

import com.photoconnect.photographer.domain.PhotographerProfile;
import com.photoconnect.photographer.dto.CreateProfileRequest;
import com.photoconnect.photographer.dto.PhotographerProfileResponse;
import com.photoconnect.photographer.dto.UpdateProfileRequest;
import com.photoconnect.photographer.exception.PhotographerExceptions.ProfileAlreadyExistsException;
import com.photoconnect.photographer.exception.PhotographerExceptions.ProfileNotFoundException;
import com.photoconnect.photographer.mapper.PhotographerMapper;
import com.photoconnect.photographer.repository.PhotographerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Core business logic for photographer profile management.
 *
 * <p>Used both by the REST controllers in this module AND directly (in-process,
 * via bean injection) by customer-service inquiry creation and reviews-service
 * review creation — the monolithic replacement for the original Feign calls.</p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PhotographerService {

    private final PhotographerProfileRepository repository;
    private final PhotographerMapper mapper;

    public PhotographerProfileResponse createProfile(UUID userId, CreateProfileRequest request) {
        if (repository.existsByUserId(userId)) {
            throw new ProfileAlreadyExistsException(userId);
        }
        PhotographerProfile profile = mapper.toEntity(request);
        profile.setUserId(userId);
        PhotographerProfile saved = repository.save(profile);
        log.info("Created photographer profile {} for user {}", saved.getId(), userId);
        return mapper.toResponse(saved);
    }

    public PhotographerProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        PhotographerProfile profile = findByUserIdOrThrow(userId);
        mapper.updateEntity(request, profile);
        return mapper.toResponse(repository.save(profile));
    }

    public void deleteProfile(UUID userId) {
        PhotographerProfile profile = findByUserIdOrThrow(userId);
        repository.delete(profile);
        log.info("Deleted photographer profile for user {}", userId);
    }

    @Transactional(readOnly = true)
    public PhotographerProfileResponse getOwnProfile(UUID userId) {
        return mapper.toResponse(findByUserIdOrThrow(userId));
    }

    @Transactional(readOnly = true)
    public PhotographerProfileResponse getProfileById(UUID profileId) {
        return mapper.toResponse(
                repository.findById(profileId)
                        .orElseThrow(() -> new ProfileNotFoundException(profileId)));
    }

    @Transactional(readOnly = true)
    public List<PhotographerProfileResponse> listAvailableProfiles() {
        return repository.findAllByAvailableTrue()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Cross-module helper used by customer-service and reviews-service.
     * Returns Optional to let callers decide how to translate the missing case.
     */
    @Transactional(readOnly = true)
    public Optional<PhotographerProfile> findEntityById(UUID profileId) {
        return repository.findById(profileId);
    }

    private PhotographerProfile findByUserIdOrThrow(UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
    }
}
