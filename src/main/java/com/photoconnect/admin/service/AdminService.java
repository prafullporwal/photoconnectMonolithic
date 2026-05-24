package com.photoconnect.admin.service;

import com.photoconnect.admin.dto.AdminContentItemResponse;
import com.photoconnect.admin.dto.AdminStatsResponse;
import com.photoconnect.admin.dto.AdminUserResponse;
import com.photoconnect.auth.domain.Role;
import com.photoconnect.auth.domain.User;
import com.photoconnect.auth.repository.UserRepository;
import com.photoconnect.customer.dto.InquiryResponse;
import com.photoconnect.customer.mapper.InquiryMapper;
import com.photoconnect.customer.repository.InquiryRepository;
import com.photoconnect.photographer.domain.PhotographerProfile;
import com.photoconnect.photographer.domain.PortfolioItem;
import com.photoconnect.photographer.dto.PhotographerProfileResponse;
import com.photoconnect.photographer.mapper.PhotographerMapper;
import com.photoconnect.photographer.repository.PhotographerProfileRepository;
import com.photoconnect.photographer.repository.PortfolioItemRepository;
import com.photoconnect.photographer.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final InquiryRepository inquiryRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final InquiryMapper inquiryMapper;
    private final PhotographerMapper photographerMapper;
    private final S3Client s3;
    private final StorageProperties storage;

    public AdminStatsResponse getStats() {
        long photographers  = userRepository.countByRoleAndDeletedAtIsNull(Role.PHOTOGRAPHER);
        long customers      = userRepository.countByRoleAndDeletedAtIsNull(Role.CUSTOMER);
        long totalUsers     = photographers + customers;
        long totalInquiries = inquiryRepository.count();
        long totalProfiles  = photographerProfileRepository.count();
        return new AdminStatsResponse(totalUsers, photographers, customers, totalInquiries, totalProfiles);
    }

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .filter(u -> u.getRole() != Role.ADMIN)
                .map(this::toAdminUserResponse)
                .toList();
    }

    @Transactional
    public AdminUserResponse setUserEnabled(UUID userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setEnabled(enabled);
        return toAdminUserResponse(userRepository.save(user));
    }

    public List<PhotographerProfileResponse> listPhotographers() {
        return photographerProfileRepository.findAll()
                .stream()
                .map(photographerMapper::toResponse)
                .toList();
    }

    public List<InquiryResponse> listInquiries() {
        return inquiryRepository.findAll()
                .stream()
                .map(inquiryMapper::toResponse)
                .toList();
    }

    // ── Content management ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AdminContentItemResponse> listAllContent() {
        List<PortfolioItem> items = portfolioItemRepository.findAll();

        // Build a profileId → displayName lookup in one extra query
        Map<UUID, String> nameByProfileId = photographerProfileRepository.findAll()
                .stream()
                .collect(Collectors.toMap(PhotographerProfile::getId, PhotographerProfile::getDisplayName));

        return items.stream()
                .map(item -> new AdminContentItemResponse(
                        item.getId(),
                        item.getPhotographerProfileId(),
                        nameByProfileId.getOrDefault(item.getPhotographerProfileId(), "Unknown"),
                        item.getMediaType(),
                        item.getCategory(),
                        item.getMimeType(),
                        item.getSizeBytes(),
                        item.getPublicUrl(),
                        item.getOriginalFileName(),
                        item.getUploadedAt()))
                .toList();
    }

    @Transactional
    public void adminDeleteContent(UUID itemId) {
        PortfolioItem item = portfolioItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio item not found: " + itemId));

        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(storage.bucket())
                .key(item.getStorageKey())
                .build());

        portfolioItemRepository.delete(item);
        log.info("Admin deleted portfolio item {} ({} bytes) for profile {}",
                itemId, item.getSizeBytes(), item.getPhotographerProfileId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AdminUserResponse toAdminUserResponse(User u) {
        return new AdminUserResponse(
                u.getId(), u.getEmail(), u.getPhone(),
                u.getRole(), u.isEnabled(), u.getCreatedAt());
    }
}
