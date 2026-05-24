package com.photoconnect.photographer.service;

import com.photoconnect.photographer.domain.MediaType;
import com.photoconnect.photographer.domain.PhotographerProfile;
import com.photoconnect.photographer.domain.PortfolioItem;
import com.photoconnect.photographer.dto.FeedItemResponse;
import com.photoconnect.photographer.dto.PortfolioItemResponse;
import com.photoconnect.photographer.exception.PhotographerExceptions.DuplicatePortfolioFileException;
import com.photoconnect.photographer.exception.PhotographerExceptions.InvalidPortfolioFileException;
import com.photoconnect.photographer.exception.PhotographerExceptions.PortfolioItemNotFoundException;
import com.photoconnect.photographer.exception.PhotographerExceptions.ProfileNotFoundException;
import com.photoconnect.photographer.repository.FeedRow;
import com.photoconnect.photographer.repository.PhotographerProfileRepository;
import com.photoconnect.photographer.repository.PortfolioItemRepository;
import com.photoconnect.photographer.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioItemRepository portfolioRepo;
    private final PhotographerProfileRepository profileRepo;
    private final S3Client s3;
    private final StorageProperties storage;

    public PortfolioItemResponse upload(UUID userId, MultipartFile file,
                                         MediaType mediaType, String category) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPortfolioFileException("Uploaded file is empty");
        }
        String mime = file.getContentType();
        if (mime == null || mime.isBlank()) {
            throw new InvalidPortfolioFileException("Missing Content-Type on uploaded file");
        }
        validateMimeForType(mediaType, mime);

        PhotographerProfile profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        String originalFileName = file.getOriginalFilename();
        if (originalFileName != null && !originalFileName.isBlank()
                && portfolioRepo.existsByPhotographerProfileIdAndOriginalFileNameAndSizeBytes(
                        profile.getId(), originalFileName, file.getSize())) {
            throw new DuplicatePortfolioFileException(originalFileName);
        }

        UUID storageId = UUID.randomUUID();
        String key = "photographers/" + profile.getId() + "/" + storageId + extensionFor(mime);

        try {
            s3.putObject(
                    PutObjectRequest.builder()
                            .bucket(storage.bucket())
                            .key(key)
                            .contentType(mime)
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new InvalidPortfolioFileException("Could not read upload stream: " + e.getMessage());
        }

        PortfolioItem item = new PortfolioItem();
        item.setPhotographerProfileId(profile.getId());
        item.setMediaType(mediaType);
        item.setCategory(category.trim().toLowerCase());
        item.setMimeType(mime);
        item.setSizeBytes(file.getSize());
        item.setStorageKey(key);
        item.setPublicUrl(buildPublicUrl(key));
        item.setOriginalFileName(originalFileName);
        item.setDisplayOrder(0);
        PortfolioItem saved = portfolioRepo.save(item);

        log.info("Uploaded portfolio item {} ({} bytes, {}) for profile {}",
                saved.getId(), saved.getSizeBytes(), saved.getMimeType(), profile.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PortfolioItemResponse> listForProfile(UUID profileId, MediaType mediaTypeFilter,
                                                       String categoryFilter) {
        List<PortfolioItem> items;
        if (mediaTypeFilter != null) {
            items = portfolioRepo
                    .findByPhotographerProfileIdAndMediaTypeOrderByDisplayOrderAscUploadedAtAsc(
                            profileId, mediaTypeFilter);
        } else if (categoryFilter != null && !categoryFilter.isBlank()) {
            items = portfolioRepo
                    .findByPhotographerProfileIdAndCategoryOrderByDisplayOrderAscUploadedAtAsc(
                            profileId, categoryFilter.trim().toLowerCase());
        } else {
            items = portfolioRepo
                    .findByPhotographerProfileIdOrderByDisplayOrderAscUploadedAtAsc(profileId);
        }
        return items.stream().map(PortfolioService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FeedItemResponse getAsFeedItem(UUID itemId) {
        FeedRow row = portfolioRepo.findFeedById(itemId)
                .orElseThrow(() -> new PortfolioItemNotFoundException(itemId));
        return toFeedResponse(row);
    }

    /** Optional variant used by favorite enrichment — returns null on missing. */
    @Transactional(readOnly = true)
    public Optional<FeedItemResponse> findAsFeedItem(UUID itemId) {
        return portfolioRepo.findFeedById(itemId).map(PortfolioService::toFeedResponse);
    }

    @Transactional(readOnly = true)
    public List<FeedItemResponse> listFeed(int limit) {
        int safe = Math.max(1, Math.min(limit, 200));
        return portfolioRepo.findFeed(Limit.of(safe)).stream()
                .map(PortfolioService::toFeedResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PortfolioItemResponse> listMine(UUID userId) {
        PhotographerProfile profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));
        return listForProfile(profile.getId(), null, null);
    }

    public void delete(UUID userId, UUID itemId) {
        PortfolioItem item = portfolioRepo.findById(itemId)
                .orElseThrow(() -> new PortfolioItemNotFoundException(itemId));

        PhotographerProfile profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException(userId));

        if (!item.getPhotographerProfileId().equals(profile.getId())) {
            throw new AccessDeniedException("You can only delete your own portfolio items");
        }

        s3.deleteObject(DeleteObjectRequest.builder()
                .bucket(storage.bucket())
                .key(item.getStorageKey())
                .build());
        portfolioRepo.delete(item);
        log.info("Deleted portfolio item {} for profile {}", itemId, profile.getId());
    }

    private String buildPublicUrl(String key) {
        String prefix = storage.publicUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            // No explicit public URL configured — use a root-relative path so the
            // browser resolves it against whatever origin is serving the app.
            // The Vite dev proxy (and any reverse-proxy in prod) must forward
            // /<bucket>/** to MinIO.
            return "/" + storage.bucket() + "/" + key;
        }
        if (prefix.endsWith("/")) prefix = prefix.substring(0, prefix.length() - 1);
        return prefix + "/" + storage.bucket() + "/" + key;
    }

    private static void validateMimeForType(MediaType type, String mime) {
        boolean ok = switch (type) {
            case IMAGE -> mime.startsWith("image/");
            case VIDEO, REEL -> mime.startsWith("video/");
        };
        if (!ok) {
            throw new InvalidPortfolioFileException(
                    "MIME type " + mime + " does not match declared media type " + type);
        }
    }

    private static String extensionFor(String mime) {
        return switch (mime) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif"  -> ".gif";
            case "video/mp4"  -> ".mp4";
            case "video/webm" -> ".webm";
            case "video/quicktime" -> ".mov";
            default -> "";
        };
    }

    private static FeedItemResponse toFeedResponse(FeedRow r) {
        return new FeedItemResponse(
                r.itemId(),
                r.mediaType(),
                r.category(),
                r.mimeType(),
                r.publicUrl(),
                r.uploadedAt(),
                new FeedItemResponse.PhotographerSnippet(
                        r.photographerProfileId(),
                        r.photographerName(),
                        r.photographerLocation()));
    }

    private static PortfolioItemResponse toResponse(PortfolioItem i) {
        return new PortfolioItemResponse(
                i.getId(),
                i.getPhotographerProfileId(),
                i.getMediaType(),
                i.getCategory(),
                i.getMimeType(),
                i.getSizeBytes(),
                i.getPublicUrl(),
                i.getDisplayOrder(),
                i.getUploadedAt(),
                i.getOriginalFileName());
    }
}
