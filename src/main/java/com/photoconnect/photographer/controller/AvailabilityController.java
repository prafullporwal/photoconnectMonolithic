package com.photoconnect.photographer.controller;

import com.photoconnect.auth.security.UserPrincipal;
import com.photoconnect.photographer.dto.AddAvailabilityRequest;
import com.photoconnect.photographer.dto.AvailabilitySlotResponse;
import com.photoconnect.photographer.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/photographers")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService service;

    @GetMapping("/me/availability")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<AvailabilitySlotResponse>> listMine(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(service.listMine(caller.userId()));
    }

    @PostMapping("/me/availability")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<AvailabilitySlotResponse>> add(
            @AuthenticationPrincipal UserPrincipal caller,
            @Valid @RequestBody AddAvailabilityRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.addBulk(caller.userId(), request));
    }

    @DeleteMapping("/me/availability/{slotId}")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal caller,
            @PathVariable UUID slotId) {
        service.delete(caller.userId(), slotId);
    }

    @DeleteMapping("/me/availability")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAll(@AuthenticationPrincipal UserPrincipal caller) {
        service.clearAll(caller.userId());
    }

    @GetMapping("/{profileId}/availability")
    @PreAuthorize("!hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<AvailabilitySlotResponse>> listForProfile(
            @PathVariable UUID profileId) {
        return ResponseEntity.ok(service.listForProfile(profileId));
    }
}
