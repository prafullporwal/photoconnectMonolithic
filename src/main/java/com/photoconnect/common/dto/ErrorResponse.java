package com.photoconnect.common.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Shared error envelope used by every module's exception handler.
 *
 * <p>In the microservice version each service had its own copy of this record;
 * the monolith consolidates them into {@code com.photoconnect.common}.</p>
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message,
                                   String path, String correlationId) {
        return new ErrorResponse(Instant.now(), status, error, message,
                path, correlationId, null);
    }

    public static ErrorResponse withFields(int status, String error, String message,
                                           String path, String correlationId,
                                           Map<String, String> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message,
                path, correlationId, fieldErrors);
    }
}
