package com.photoconnect.customer.controller;

import com.photoconnect.auth.security.UserPrincipal;
import com.photoconnect.customer.dto.CreateInquiryRequest;
import com.photoconnect.customer.dto.InquiryResponse;
import com.photoconnect.customer.dto.UpdateInquiryStatusRequest;
import com.photoconnect.customer.service.InquiryService;
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
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService service;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<InquiryResponse> createInquiry(
            @AuthenticationPrincipal UserPrincipal caller,
            @Valid @RequestBody CreateInquiryRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createInquiry(caller.userId(), request));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<InquiryResponse>> myInquiries(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(service.listMyInquiries(caller.userId()));
    }

    @GetMapping("/received")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<InquiryResponse>> receivedInquiries(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(service.listReceivedInquiries(caller.userId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InquiryResponse> getInquiry(
            @AuthenticationPrincipal UserPrincipal caller,
            @PathVariable UUID id) {
        return ResponseEntity.ok(service.getInquiry(id, caller.userId()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<InquiryResponse> updateStatus(
            @AuthenticationPrincipal UserPrincipal caller,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInquiryStatusRequest request) {
        return ResponseEntity.ok(service.updateStatus(id, caller.userId(), request.status()));
    }
}
