package com.photoconnect.customer.exception;

import com.photoconnect.common.exception.DomainException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

public final class CustomerExceptions {

    private CustomerExceptions() {}

    public static class CustomerNotFoundException extends DomainException {
        public CustomerNotFoundException(UUID id) {
            super(HttpStatus.NOT_FOUND, "Customer not found: " + id);
        }
    }

    public static class CustomerAlreadyExistsException extends DomainException {
        public CustomerAlreadyExistsException(UUID userId) {
            super(HttpStatus.CONFLICT, "User already has a customer profile: " + userId);
        }
    }

    public static class InquiryNotFoundException extends DomainException {
        public InquiryNotFoundException(UUID id) {
            super(HttpStatus.NOT_FOUND, "Inquiry not found: " + id);
        }
    }

    public static class InquiryAccessDeniedException extends DomainException {
        public InquiryAccessDeniedException() {
            super(HttpStatus.FORBIDDEN, "You are not a participant on this inquiry");
        }
    }

    public static class PhotographerNotFoundException extends DomainException {
        public PhotographerNotFoundException(UUID id) {
            super(HttpStatus.BAD_REQUEST, "Photographer or portfolio item not found: " + id);
        }
    }

    public static class DateUnavailableException extends DomainException {
        public DateUnavailableException(LocalDate date) {
            super(HttpStatus.BAD_REQUEST,
                    "Photographer is not available on " + date);
        }
    }
}
