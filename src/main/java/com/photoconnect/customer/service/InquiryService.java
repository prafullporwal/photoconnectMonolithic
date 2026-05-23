package com.photoconnect.customer.service;

import com.photoconnect.customer.domain.Inquiry;
import com.photoconnect.customer.domain.InquiryStatus;
import com.photoconnect.customer.dto.CreateInquiryRequest;
import com.photoconnect.customer.dto.InquiryResponse;
import com.photoconnect.customer.exception.CustomerExceptions.DateUnavailableException;
import com.photoconnect.customer.exception.CustomerExceptions.InquiryAccessDeniedException;
import com.photoconnect.customer.exception.CustomerExceptions.InquiryNotFoundException;
import com.photoconnect.customer.exception.CustomerExceptions.PhotographerNotFoundException;
import com.photoconnect.customer.mapper.InquiryMapper;
import com.photoconnect.customer.repository.InquiryRepository;
import com.photoconnect.photographer.domain.PhotographerProfile;
import com.photoconnect.photographer.dto.AvailabilitySlotResponse;
import com.photoconnect.photographer.service.AvailabilityService;
import com.photoconnect.photographer.service.PhotographerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Business logic for inquiries.
 *
 * <p>The microservices version used a Feign client to call photographer-service
 * to (a) verify the photographer exists and (b) capture the photographer's
 * userId for inbox queries. In the monolith we inject {@link PhotographerService}
 * directly — same logical operation, but a plain in-process bean call instead
 * of an HTTP round-trip. No more {@code FeignException}, no more circuit
 * breakers, no service tokens.</p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository repository;
    private final InquiryMapper mapper;
    private final PhotographerService photographerService;
    private final AvailabilityService availabilityService;

    public InquiryResponse createInquiry(UUID customerId, CreateInquiryRequest request) {

        // ── Verify the photographer exists (was a Feign call, now in-process)
        PhotographerProfile photographer = photographerService
                .findEntityById(request.photographerProfileId())
                .orElseThrow(() -> new PhotographerNotFoundException(request.photographerProfileId()));

        // ── Confirm the date is on the photographer's posted calendar
        //    (skipped if no calendar is posted — open-to-any-date semantics).
        verifyDateIsAvailable(photographer.getId(), request.eventDate());

        Inquiry inquiry = new Inquiry();
        inquiry.setCustomerId(customerId);
        inquiry.setPhotographerProfileId(photographer.getId());
        inquiry.setPhotographerUserId(photographer.getUserId());
        inquiry.setEventDate(request.eventDate());
        inquiry.setEventType(request.eventType());
        inquiry.setLocation(request.location());
        inquiry.setBudget(request.budget());
        inquiry.setMessage(request.message());
        inquiry.setStatus(InquiryStatus.NEW);

        Inquiry saved = repository.save(inquiry);
        log.info("Created inquiry {} from customer {} to photographer {} (user {})",
                saved.getId(), customerId, photographer.getId(), photographer.getUserId());
        return mapper.toResponse(saved);
    }

    public InquiryResponse updateStatus(UUID inquiryId, UUID callerUserId, InquiryStatus newStatus) {
        Inquiry inquiry = repository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException(inquiryId));

        if (!isParticipant(inquiry, callerUserId)) {
            throw new InquiryAccessDeniedException();
        }

        inquiry.setStatus(newStatus);
        return mapper.toResponse(repository.save(inquiry));
    }

    @Transactional(readOnly = true)
    public InquiryResponse getInquiry(UUID inquiryId, UUID callerUserId) {
        Inquiry inquiry = repository.findById(inquiryId)
                .orElseThrow(() -> new InquiryNotFoundException(inquiryId));

        if (!isParticipant(inquiry, callerUserId)) {
            throw new InquiryAccessDeniedException();
        }
        return mapper.toResponse(inquiry);
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> listMyInquiries(UUID customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InquiryResponse> listReceivedInquiries(UUID photographerUserId) {
        return repository.findByPhotographerUserIdOrderByCreatedAtDesc(photographerUserId)
                .stream().map(mapper::toResponse).toList();
    }

    /**
     * Cross-module lookup used by reviews module: "did this customer complete
     * a booking with this photographer?". Replaces the customer-service
     * /internal/v1/inquiries/completed endpoint.
     */
    @Transactional(readOnly = true)
    public java.util.Optional<Inquiry> findLatestCompletedEngagement(UUID customerId, UUID photographerProfileId) {
        return repository.findFirstByCustomerIdAndPhotographerProfileIdAndStatusOrderByUpdatedAtDesc(
                customerId, photographerProfileId, InquiryStatus.COMPLETED);
    }

    private void verifyDateIsAvailable(UUID photographerProfileId, LocalDate requestedDate) {
        List<AvailabilitySlotResponse> calendar = availabilityService.listForProfile(photographerProfileId);
        if (calendar.isEmpty()) {
            return;
        }
        boolean dateMatches = calendar.stream()
                .anyMatch(slot -> requestedDate.equals(slot.availableDate()));
        if (!dateMatches) {
            throw new DateUnavailableException(requestedDate);
        }
    }

    private static boolean isParticipant(Inquiry inquiry, UUID callerUserId) {
        return inquiry.getCustomerId().equals(callerUserId)
                || inquiry.getPhotographerUserId().equals(callerUserId);
    }
}
