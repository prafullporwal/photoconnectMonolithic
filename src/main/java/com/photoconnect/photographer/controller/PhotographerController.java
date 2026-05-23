package com.photoconnect.photographer.controller;

import com.photoconnect.auth.security.UserPrincipal;
import com.photoconnect.photographer.dto.CreateProfileRequest;
import com.photoconnect.photographer.dto.PhotographerProfileResponse;
import com.photoconnect.photographer.dto.UpdateProfileRequest;
import com.photoconnect.photographer.service.PhotographerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for photographer profile operations.
 *
 * <p>Uses {@link UserPrincipal} (the JWT-derived principal) instead of the
 * microservice-era {@code GatewayPrincipal} — there's no gateway, so identity
 * comes straight from the validated JWT.</p>
 */
@RestController
@RequestMapping("/api/v1/photographers")
@RequiredArgsConstructor
public class PhotographerController {

    private final PhotographerService service;

    /** Browse photographers available for bookings. Hidden from logged-in photographers. */
    @GetMapping
    @PreAuthorize("!hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<PhotographerProfileResponse>> listPhotographers() {
        return ResponseEntity.ok(service.listAvailableProfiles());
    }

    /** Get the authenticated photographer's own profile. Order matters — /me before /{id}. */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<PhotographerProfileResponse> getOwnProfile(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(service.getOwnProfile(caller.userId()));
    }

    @PostMapping("/me")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<PhotographerProfileResponse> createProfile(
            @AuthenticationPrincipal UserPrincipal caller,
            @Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createProfile(caller.userId(), request));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<PhotographerProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal caller,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(service.updateProfile(caller.userId(), request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal UserPrincipal caller) {
        service.deleteProfile(caller.userId());
        return ResponseEntity.noContent().build();
    }

    /** View a specific photographer's profile. Hidden from logged-in photographers. */
    @GetMapping("/{id}")
    @PreAuthorize("!hasRole('PHOTOGRAPHER')")
    public ResponseEntity<PhotographerProfileResponse> getPhotographer(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getProfileById(id));
    }
}
