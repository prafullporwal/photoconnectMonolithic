package com.photoconnect.photographer.controller;

import com.photoconnect.auth.security.UserPrincipal;
import com.photoconnect.photographer.domain.MediaType;
import com.photoconnect.photographer.dto.FeedItemResponse;
import com.photoconnect.photographer.dto.PortfolioItemResponse;
import com.photoconnect.photographer.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/photographers")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService service;

    @PostMapping(value = "/me/portfolio", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<PortfolioItemResponse> upload(
            @AuthenticationPrincipal UserPrincipal caller,
            @RequestParam("file") MultipartFile file,
            @RequestParam("mediaType") MediaType mediaType,
            @RequestParam("category") String category) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.upload(caller.userId(), file, mediaType, category));
    }

    @GetMapping("/me/portfolio")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<PortfolioItemResponse>> listMine(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(service.listMine(caller.userId()));
    }

    @DeleteMapping("/me/portfolio/{itemId}")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal caller,
            @PathVariable UUID itemId) {
        service.delete(caller.userId(), itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed")
    @PreAuthorize("!hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<FeedItemResponse>> feed(
            @RequestParam(required = false, defaultValue = "50") int limit) {
        return ResponseEntity.ok(service.listFeed(limit));
    }

    @GetMapping("/{profileId}/portfolio")
    @PreAuthorize("!hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<PortfolioItemResponse>> listForProfile(
            @PathVariable UUID profileId,
            @RequestParam(required = false) MediaType mediaType,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(service.listForProfile(profileId, mediaType, category));
    }
}
