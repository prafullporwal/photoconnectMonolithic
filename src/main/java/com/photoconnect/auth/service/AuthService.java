package com.photoconnect.auth.service;

import com.photoconnect.auth.domain.RefreshToken;
import com.photoconnect.auth.domain.Role;
import com.photoconnect.auth.domain.User;
import com.photoconnect.auth.dto.AuthResponse;
import com.photoconnect.auth.dto.LoginRequest;
import com.photoconnect.auth.dto.RefreshRequest;
import com.photoconnect.auth.dto.RegisterRequest;
import com.photoconnect.auth.exception.AuthExceptions.EmailAlreadyExistsException;
import com.photoconnect.auth.exception.AuthExceptions.InvalidCredentialsException;
import com.photoconnect.auth.exception.AuthExceptions.InvalidTokenException;
import com.photoconnect.auth.repository.RefreshTokenRepository;
import com.photoconnect.auth.repository.UserRepository;
import com.photoconnect.auth.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Business logic for register, login, OTP verify-or-create, refresh (with
 * rotation), and logout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService blacklistService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(req.email())) {
            throw new EmailAlreadyExistsException(req.email());
        }
        User user = User.builder()
                .email(req.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(req.role())
                .enabled(true)
                .build();
        user = userRepository.save(user);
        log.info("Registered user id={} email={} role={}", user.getId(), user.getEmail(), user.getRole());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(req.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }
        if (user.getPasswordHash() == null) {
            throw new InvalidCredentialsException();
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        log.info("Login success user id={} email={}", user.getId(), user.getEmail());
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse registerOrLoginViaOtp(String phone, String email, Role role) {
        User user = userRepository.findByPhoneAndDeletedAtIsNull(phone).orElse(null);
        if (user == null) {
            User.UserBuilder builder = User.builder()
                    .phone(phone)
                    .role(role)
                    .enabled(true);
            if (email != null && !email.isBlank()) {
                builder.email(email.toLowerCase());
            }
            user = userRepository.save(builder.build());
            log.info("Registered OTP user id={} phone={} role={}", user.getId(), phone, role);
        } else {
            log.info("OTP login user id={} phone={}", user.getId(), phone);
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        Claims claims;
        try {
            claims = jwtService.parseAndVerify(req.refreshToken()).getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException(ex.getMessage());
        }
        if (!JwtService.TYP_REFRESH.equals(claims.get(JwtService.CLAIM_TYP, String.class))) {
            throw new InvalidTokenException("not a refresh token");
        }

        String jti = claims.getId();
        RefreshToken stored = refreshTokenRepository.findByTokenId(jti)
                .orElseThrow(() -> new InvalidTokenException("unknown refresh token"));

        if (stored.isRevoked()) {
            log.warn("Refresh-token re-use detected for user {}; revoking all sessions", stored.getUserId());
            refreshTokenRepository.revokeAllForUser(stored.getUserId());
            throw new InvalidTokenException("refresh token already used");
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("refresh token expired");
        }

        refreshTokenRepository.revokeByTokenId(jti);

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new InvalidTokenException("user no longer exists"));
        return issueTokens(user);
    }

    @Transactional
    public void logout(UUID userId, String accessJti, Instant accessExpiresAt) {
        refreshTokenRepository.revokeAllForUser(userId);
        blacklistService.blacklist(accessJti, accessExpiresAt);
        log.info("Logged out user id={}", userId);
    }

    private AuthResponse issueTokens(User user) {
        JwtService.IssuedToken access  = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        JwtService.IssuedToken refresh = jwtService.generateRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenId(refresh.jti())
                .expiresAt(refresh.expiresAt())
                .revoked(false)
                .createdAt(Instant.now())
                .build());

        return new AuthResponse(
                access.token(),
                refresh.token(),
                access.expiresAt(),
                user.getId(),
                user.getEmail(),
                user.getRole());
    }
}
