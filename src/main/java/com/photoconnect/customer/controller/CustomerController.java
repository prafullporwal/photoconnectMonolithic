package com.photoconnect.customer.controller;

import com.photoconnect.auth.security.UserPrincipal;
import com.photoconnect.customer.dto.CreateCustomerRequest;
import com.photoconnect.customer.dto.CustomerResponse;
import com.photoconnect.customer.dto.UpdateCustomerRequest;
import com.photoconnect.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponse> getOwnProfile(
            @AuthenticationPrincipal UserPrincipal caller) {
        return ResponseEntity.ok(service.getOwnProfile(caller.userId()));
    }

    @PostMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponse> createProfile(
            @AuthenticationPrincipal UserPrincipal caller,
            @Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createProfile(caller.userId(), request));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal caller,
            @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(service.updateProfile(caller.userId(), request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal UserPrincipal caller) {
        service.deleteProfile(caller.userId());
        return ResponseEntity.noContent().build();
    }
}
