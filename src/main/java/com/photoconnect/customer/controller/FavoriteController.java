package com.photoconnect.customer.controller;

import com.photoconnect.auth.security.UserPrincipal;
import com.photoconnect.customer.dto.FavoriteResponse;
import com.photoconnect.customer.dto.FavoriteStatusResponse;
import com.photoconnect.customer.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService service;

    @PutMapping("/{portfolioItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FavoriteResponse> save(
            @AuthenticationPrincipal UserPrincipal caller,
            @PathVariable UUID portfolioItemId) {
        return ResponseEntity.ok(service.save(caller.userId(), portfolioItemId));
    }

    @DeleteMapping("/{portfolioItemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @AuthenticationPrincipal UserPrincipal caller,
            @PathVariable UUID portfolioItemId) {
        service.remove(caller.userId(), portfolioItemId);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<FavoriteResponse>> list(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(service.list(caller.userId()));
    }

    @GetMapping("/{portfolioItemId}/status")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FavoriteStatusResponse> status(
            @AuthenticationPrincipal UserPrincipal caller,
            @PathVariable UUID portfolioItemId) {
        return ResponseEntity.ok(service.getStatus(caller.userId(), portfolioItemId));
    }
}
