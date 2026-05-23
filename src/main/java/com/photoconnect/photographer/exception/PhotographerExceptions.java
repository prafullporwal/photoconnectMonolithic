package com.photoconnect.photographer.exception;

import com.photoconnect.common.exception.DomainException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public final class PhotographerExceptions {

    private PhotographerExceptions() {}

    public static class ProfileNotFoundException extends DomainException {
        public ProfileNotFoundException(UUID id) {
            super(HttpStatus.NOT_FOUND, "Photographer profile not found: " + id);
        }
    }

    public static class ProfileAlreadyExistsException extends DomainException {
        public ProfileAlreadyExistsException(UUID userId) {
            super(HttpStatus.CONFLICT, "User already has a photographer profile: " + userId);
        }
    }

    public static class PortfolioItemNotFoundException extends DomainException {
        public PortfolioItemNotFoundException(UUID id) {
            super(HttpStatus.NOT_FOUND, "Portfolio item not found: " + id);
        }
    }

    public static class InvalidPortfolioFileException extends DomainException {
        public InvalidPortfolioFileException(String reason) {
            super(HttpStatus.BAD_REQUEST, reason);
        }
    }

    public static class AvailabilitySlotNotFoundException extends DomainException {
        public AvailabilitySlotNotFoundException(UUID id) {
            super(HttpStatus.NOT_FOUND, "Availability slot not found: " + id);
        }
    }
}
