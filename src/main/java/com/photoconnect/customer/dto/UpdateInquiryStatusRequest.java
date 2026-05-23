package com.photoconnect.customer.dto;

import com.photoconnect.customer.domain.InquiryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateInquiryStatusRequest(
        @NotNull InquiryStatus status
) {}
