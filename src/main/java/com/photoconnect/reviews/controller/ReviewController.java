package com.photoconnect.reviews.controller;

import com.photoconnect.auth.security.UserPrincipal;
import com.photoconnect.reviews.dto.CreateReviewRequest;
import com.photoconnect.reviews.dto.ReviewResponse;
import com.photoconnect.reviews.dto.ReviewSummaryResponse;
import com.photoconnect.reviews.service.ReviewService;
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
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> createReview(
            @AuthenticationPrincipal UserPrincipal caller,
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createReview(caller.userId(), request));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ReviewResponse>> myReviews(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(service.listMine(caller.userId()));
    }

    @GetMapping("/photographer/{profileId}")
    public ResponseEntity<List<ReviewResponse>> reviewsForPhotographer(
            @PathVariable UUID profileId) {
        return ResponseEntity.ok(service.listForPhotographer(profileId));
    }

    @GetMapping("/summary/{profileId}")
    public ResponseEntity<ReviewSummaryResponse> photographerSummary(
            @PathVariable UUID profileId) {
        return ResponseEntity.ok(service.summarise(profileId));
    }
}
