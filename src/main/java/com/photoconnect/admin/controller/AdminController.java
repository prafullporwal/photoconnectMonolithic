package com.photoconnect.admin.controller;

import com.photoconnect.admin.dto.AdminContentItemResponse;
import com.photoconnect.admin.dto.AdminStatsResponse;
import com.photoconnect.admin.dto.AdminUserResponse;
import com.photoconnect.admin.service.AdminService;
import com.photoconnect.customer.dto.InquiryResponse;
import com.photoconnect.photographer.dto.PhotographerProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PatchMapping("/users/{id}/enable")
    public ResponseEntity<AdminUserResponse> enableUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.setUserEnabled(id, true));
    }

    @PatchMapping("/users/{id}/disable")
    public ResponseEntity<AdminUserResponse> disableUser(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.setUserEnabled(id, false));
    }

    @GetMapping("/photographers")
    public ResponseEntity<List<PhotographerProfileResponse>> listPhotographers() {
        return ResponseEntity.ok(adminService.listPhotographers());
    }

    @GetMapping("/inquiries")
    public ResponseEntity<List<InquiryResponse>> listInquiries() {
        return ResponseEntity.ok(adminService.listInquiries());
    }

    @GetMapping("/content")
    public ResponseEntity<List<AdminContentItemResponse>> listContent() {
        return ResponseEntity.ok(adminService.listAllContent());
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable UUID id) {
        adminService.adminDeleteContent(id);
        return ResponseEntity.noContent().build();
    }
}
