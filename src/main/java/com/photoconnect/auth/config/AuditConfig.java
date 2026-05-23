package com.photoconnect.auth.config;

import com.photoconnect.auth.security.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Enables Spring Data JPA Auditing and supplies the current user UUID
 * for {@code @CreatedBy} / {@code @LastModifiedBy} on {@link com.photoconnect.auth.domain.AuditableEntity}.
 */
@Configuration
@EnableJpaAuditing
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
                return Optional.of("system");
            }
            if (auth.getPrincipal() instanceof UserPrincipal up) {
                return Optional.of(up.userId().toString());
            }
            return Optional.of(auth.getName());
        };
    }
}
