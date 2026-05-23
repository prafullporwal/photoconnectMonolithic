package com.photoconnect.auth.exception;

import com.photoconnect.common.exception.DomainException;
import org.springframework.http.HttpStatus;

/**
 * Auth-module domain exceptions, grouped in a single file because each is a
 * trivial subclass with no logic of its own.
 */
public final class AuthExceptions {

    private AuthExceptions() {}

    public static class EmailAlreadyExistsException extends DomainException {
        public EmailAlreadyExistsException(String email) {
            super(HttpStatus.CONFLICT, "Email already registered: " + email);
        }
    }

    public static class PhoneAlreadyRegisteredException extends DomainException {
        public PhoneAlreadyRegisteredException(String phone) {
            super(HttpStatus.CONFLICT, "Phone already registered: " + phone);
        }
    }

    public static class InvalidCredentialsException extends DomainException {
        public InvalidCredentialsException() {
            super(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
    }

    public static class InvalidTokenException extends DomainException {
        public InvalidTokenException(String reason) {
            super(HttpStatus.UNAUTHORIZED, reason);
        }
    }

    public static class InvalidOtpException extends DomainException {
        public InvalidOtpException(String reason) {
            super(HttpStatus.UNAUTHORIZED, reason);
        }
    }

    public static class OtpCooldownException extends DomainException {
        public OtpCooldownException(long remainingSeconds) {
            super(HttpStatus.TOO_MANY_REQUESTS,
                    "OTP cooldown active; retry in " + remainingSeconds + "s");
        }
    }

    public static class OtpDeliveryException extends DomainException {
        public OtpDeliveryException(String reason) {
            super(HttpStatus.BAD_GATEWAY, "OTP delivery failed: " + reason);
        }
    }
}
