package com.photoconnect.auth.controller;

import com.photoconnect.auth.config.OtpProperties;
import com.photoconnect.auth.domain.User;
import com.photoconnect.auth.dto.AuthResponse;
import com.photoconnect.auth.dto.LoginRequest;
import com.photoconnect.auth.dto.RefreshRequest;
import com.photoconnect.auth.dto.RegisterRequest;
import com.photoconnect.auth.dto.SendOtpRequest;
import com.photoconnect.auth.dto.SendOtpResponse;
import com.photoconnect.auth.dto.UserDto;
import com.photoconnect.auth.dto.VerifyOtpRequest;
import com.photoconnect.auth.exception.AuthExceptions.InvalidTokenException;
import com.photoconnect.auth.mapper.UserMapper;
import com.photoconnect.auth.repository.UserRepository;
import com.photoconnect.auth.security.JwtService;
import com.photoconnect.auth.security.UserPrincipal;
import com.photoconnect.auth.service.AuthService;
import com.photoconnect.auth.service.OtpService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final OtpService otpService;
    private final OtpProperties otpProperties;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest body) {
        return authService.register(body);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest body) {
        return authService.login(body);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest body) {
        return authService.refresh(body);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal UserPrincipal principal,
                       HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new InvalidTokenException("missing bearer token");
        }
        Claims claims = jwtService.parseAndVerify(header.substring("Bearer ".length())).getPayload();
        Instant expiresAt = claims.getExpiration().toInstant();
        authService.logout(principal.userId(), principal.jti(), expiresAt);
    }

    @PostMapping("/otp/send")
    public SendOtpResponse sendOtp(@Valid @RequestBody SendOtpRequest body) {
        OtpService.Issued issued = otpService.sendOtp(body.phone());
        String devCode = otpProperties.devMode() ? issued.code() : null;
        return new SendOtpResponse(body.phone(), issued.expiresAt(), devCode);
    }

    @PostMapping("/otp/verify")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest body) {
        otpService.verify(body.phone(), body.code());
        return authService.registerOrLoginViaOtp(body.phone(), body.email(), body.role());
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> new InvalidTokenException("user no longer exists"));
        return ResponseEntity.ok(userMapper.toDto(user));
    }
}
