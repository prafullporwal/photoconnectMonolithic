package com.photoconnect.reviews.service;

import com.photoconnect.customer.domain.Inquiry;
import com.photoconnect.customer.service.InquiryService;
import com.photoconnect.photographer.domain.PhotographerProfile;
import com.photoconnect.photographer.service.PhotographerService;
import com.photoconnect.reviews.domain.Review;
import com.photoconnect.reviews.dto.CreateReviewRequest;
import com.photoconnect.reviews.dto.ReviewResponse;
import com.photoconnect.reviews.dto.ReviewSummaryResponse;
import com.photoconnect.reviews.exception.ReviewExceptions.DuplicateReviewException;
import com.photoconnect.reviews.exception.ReviewExceptions.NoCompletedBookingException;
import com.photoconnect.reviews.exception.ReviewExceptions.PhotographerNotFoundException;
import com.photoconnect.reviews.mapper.ReviewMapper;
import com.photoconnect.reviews.repository.ReviewRepository;
import com.photoconnect.reviews.repository.ReviewSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Core business logic for reviews.
 *
 * <p>Two cross-module dependencies replace the original Feign clients:</p>
 * <ul>
 *   <li>{@link PhotographerService#findEntityById(UUID)} replaces the
 *       photographer-service Feign call.</li>
 *   <li>{@link InquiryService#findLatestCompletedEngagement(UUID, UUID)}
 *       replaces the customer-service {@code /internal/v1/inquiries/completed}
 *       Feign call.</li>
 * </ul>
 *
 * <p>The "one review per (customer, photographer)" rule is enforced both at
 * the application layer (cheap dedup check) and at the DB layer (UNIQUE
 * constraint) — the DB wins any race.</p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final PhotographerService photographerService;
    private final InquiryService inquiryService;

    public ReviewResponse createReview(UUID customerId, CreateReviewRequest request) {
        UUID photographerProfileId = request.photographerProfileId();

        // 1. Cheap dedup check
        if (repository.existsByCustomerIdAndPhotographerProfileId(customerId, photographerProfileId)) {
            throw new DuplicateReviewException(customerId, photographerProfileId);
        }

        // 2. Validate photographer exists + capture their userId
        PhotographerProfile photographer = photographerService.findEntityById(photographerProfileId)
                .orElseThrow(() -> new PhotographerNotFoundException(photographerProfileId));

        // 3. Enforce the policy: a COMPLETED inquiry must exist
        Inquiry engagement = inquiryService
                .findLatestCompletedEngagement(customerId, photographerProfileId)
                .orElseThrow(() -> new NoCompletedBookingException(customerId, photographerProfileId));

        // 4. Persist
        Review review = new Review();
        review.setCustomerId(customerId);
        review.setPhotographerProfileId(photographer.getId());
        review.setPhotographerUserId(photographer.getUserId());
        review.setInquiryId(engagement.getId());
        review.setRating(request.rating().shortValue());
        review.setBody(request.body());

        Review saved = repository.save(review);
        log.info("Created review {} by customer {} for photographer {} (rating={}, inquiry={})",
                saved.getId(), customerId, photographer.getId(), saved.getRating(), engagement.getId());
        return mapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listForPhotographer(UUID photographerProfileId) {
        return repository.findByPhotographerProfileIdOrderByCreatedAtDesc(photographerProfileId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listMine(UUID customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse summarise(UUID photographerProfileId) {
        ReviewSummary summary = repository.summarise(photographerProfileId);
        double avg = summary == null || summary.getAverageRating() == null
                ? 0.0
                : summary.getAverageRating();
        long count = summary == null ? 0L : summary.getReviewCount();
        return new ReviewSummaryResponse(photographerProfileId, avg, count);
    }
}
