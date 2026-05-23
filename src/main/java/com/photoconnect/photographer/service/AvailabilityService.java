package com.photoconnect.photographer.service;

import com.photoconnect.photographer.domain.AvailabilitySlot;
import com.photoconnect.photographer.domain.PhotographerProfile;
import com.photoconnect.photographer.dto.AddAvailabilityRequest;
import com.photoconnect.photographer.dto.AvailabilitySlotResponse;
import com.photoconnect.photographer.exception.PhotographerExceptions.AvailabilitySlotNotFoundException;
import com.photoconnect.photographer.exception.PhotographerExceptions.ProfileNotFoundException;
import com.photoconnect.photographer.repository.AvailabilitySlotRepository;
import com.photoconnect.photographer.repository.PhotographerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilitySlotRepository slotRepo;
    private final PhotographerProfileRepository profileRepo;

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> listMine(UUID userId) {
        PhotographerProfile profile = resolveProfile(userId);
        return slotRepo
                .findByPhotographerProfileIdOrderByAvailableDateAsc(profile.getId())
                .stream()
                .map(AvailabilityService::toResponse)
                .toList();
    }

    public List<AvailabilitySlotResponse> addBulk(UUID userId, AddAvailabilityRequest request) {
        PhotographerProfile profile = resolveProfile(userId);
        LocalDate today = LocalDate.now();
        List<AvailabilitySlot> inserted = new ArrayList<>();

        for (LocalDate date : request.dates().stream().distinct().toList()) {
            if (date.isBefore(today)) {
                continue;
            }
            if (slotRepo.existsByPhotographerProfileIdAndAvailableDate(profile.getId(), date)) {
                continue;
            }
            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setPhotographerProfileId(profile.getId());
            slot.setAvailableDate(date);
            slot.setNote(request.note());
            try {
                inserted.add(slotRepo.saveAndFlush(slot));
            } catch (DataIntegrityViolationException race) {
                log.debug("Concurrent add for ({}, {}) — skipping", profile.getId(), date);
            }
        }

        if (!inserted.isEmpty()) {
            log.info("Photographer {} added {} new availability slots",
                    profile.getId(), inserted.size());
        }
        return slotRepo
                .findByPhotographerProfileIdOrderByAvailableDateAsc(profile.getId())
                .stream()
                .map(AvailabilityService::toResponse)
                .toList();
    }

    public void delete(UUID userId, UUID slotId) {
        PhotographerProfile profile = resolveProfile(userId);
        int removed = slotRepo.deleteByOwnedId(profile.getId(), slotId);
        if (removed == 0) {
            throw new AvailabilitySlotNotFoundException(slotId);
        }
        log.info("Photographer {} removed availability slot {}", profile.getId(), slotId);
    }

    public void clearAll(UUID userId) {
        PhotographerProfile profile = resolveProfile(userId);
        int removed = slotRepo.deleteAllForProfile(profile.getId());
        log.info("Photographer {} cleared {} availability slots", profile.getId(), removed);
    }

    @Transactional(readOnly = true)
    public List<AvailabilitySlotResponse> listForProfile(UUID photographerProfileId) {
        return slotRepo
                .findByPhotographerProfileIdOrderByAvailableDateAsc(photographerProfileId)
                .stream()
                .map(AvailabilityService::toResponse)
                .toList();
    }

    private PhotographerProfile resolveProfile(UUID userId) {
        return profileRepo.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
    }

    private static AvailabilitySlotResponse toResponse(AvailabilitySlot s) {
        return new AvailabilitySlotResponse(
                s.getId(),
                s.getPhotographerProfileId(),
                s.getAvailableDate(),
                s.getNote(),
                s.getCreatedAt());
    }
}
