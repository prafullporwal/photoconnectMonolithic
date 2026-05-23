package com.photoconnect.auth.service;

/**
 * Abstraction over how an OTP reaches the user.
 *
 * <p>The monolith only ships the {@link DevModeOtpDeliveryService} which logs
 * the code to stdout. Adding a real provider (MSG91, Twilio) is a single
 * {@code @Service} class behind the same interface.</p>
 */
public interface OtpDeliveryService {

    void deliver(String phone, String code);
}
