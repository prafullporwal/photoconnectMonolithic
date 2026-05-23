package com.photoconnect.customer.repository;

import com.photoconnect.customer.domain.Inquiry;
import com.photoconnect.customer.domain.InquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InquiryRepository extends JpaRepository<Inquiry, UUID> {

    List<Inquiry> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Inquiry> findByPhotographerUserIdOrderByCreatedAtDesc(UUID photographerUserId);

    Optional<Inquiry> findFirstByCustomerIdAndPhotographerProfileIdAndStatusOrderByUpdatedAtDesc(
            UUID customerId, UUID photographerProfileId, InquiryStatus status);
}
