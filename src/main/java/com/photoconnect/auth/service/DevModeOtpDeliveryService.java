package com.photoconnect.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Dev OTP delivery: prints the code to the application log.
 *
 * <p>{@code matchIfMissing = true} means dev is the default if the property is
 * missing entirely — safer than failing to boot.</p>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.otp.provider", havingValue = "dev", matchIfMissing = true)
public class DevModeOtpDeliveryService implements OtpDeliveryService {

    @Override
    public void deliver(String phone, String code) {
        log.info("[DEV-OTP] phone={} code={} — DO NOT ENABLE IN PROD", phone, code);
    }
}
