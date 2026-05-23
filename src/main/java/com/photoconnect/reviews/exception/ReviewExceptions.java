package com.photoconnect.reviews.exception;

import com.photoconnect.common.exception.DomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public final class ReviewExceptions {

    private ReviewExceptions() {}

    public static class ReviewNotFoundException extends DomainException {
        public ReviewNotFoundException(UUID id) {
            super(HttpStatus.NOT_FOUND, "Review not found: " + id);
        }
    }

    public static class DuplicateReviewException extends DomainException {
        public DuplicateReviewException(UUID customerId, UUID photographerProfileId) {
            super(HttpStatus.CONFLICT,
                    "Customer " + customerId + " has already reviewed photographer " + photographerProfileId);
        }
    }

    public static class NoCompletedBookingException extends DomainException {
        public NoCompletedBookingException(UUID customerId, UUID photographerProfileId) {
            super(HttpStatus.FORBIDDEN,
                    "No completed booking between customer " + customerId
                            + " and photographer " + photographerProfileId);
        }
    }

    public static class PhotographerNotFoundException extends DomainException {
        public PhotographerNotFoundException(UUID id) {
            super(HttpStatus.NOT_FOUND, "Photographer not found: " + id);
        }
    }
}
