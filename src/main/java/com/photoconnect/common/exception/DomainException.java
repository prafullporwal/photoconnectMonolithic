package com.photoconnect.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for business-rule exceptions across the monolith.
 *
 * <p>Each subclass declares the HTTP status it should map to;
 * {@code GlobalExceptionHandler} reads it directly so handlers stay short.</p>
 */
public abstract class DomainException extends RuntimeException {

    private final HttpStatus status;

    protected DomainException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
