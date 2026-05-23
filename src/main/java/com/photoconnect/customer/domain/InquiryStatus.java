package com.photoconnect.customer.domain;

/**
 * Inquiry lifecycle states.
 *
 * <p>{@link #COMPLETED} is the precondition reviews module checks before
 * allowing a customer to leave a review.</p>
 */
public enum InquiryStatus {
    NEW,
    READ,
    RESPONDED,
    CLOSED,
    COMPLETED
}
