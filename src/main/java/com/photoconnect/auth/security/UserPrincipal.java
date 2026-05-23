package com.photoconnect.auth.security;

import com.photoconnect.auth.domain.Role;

import java.util.UUID;

/**
 * Authenticated-user payload attached to the SecurityContext, reachable from
 * any controller (auth, photographer, customer, reviews) via
 * {@code @AuthenticationPrincipal UserPrincipal principal}.
 *
 * <p>This single principal class replaces the per-service {@code GatewayPrincipal}
 * used in the microservices version. There's no gateway-vs-service distinction
 * anymore — there is exactly one filter chain, one JWT, one principal.</p>
 */
public record UserPrincipal(UUID userId, String email, Role role, String jti) {}
