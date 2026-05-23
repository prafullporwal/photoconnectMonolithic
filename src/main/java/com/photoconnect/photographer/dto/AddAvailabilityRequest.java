package com.photoconnect.photographer.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record AddAvailabilityRequest(
        @NotNull
        @NotEmpty(message = "At least one date is required")
        List<LocalDate> dates,

        @Size(max = 200, message = "Note must be 200 characters or fewer")
        String note
) {}
