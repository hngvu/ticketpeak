package io.qzz.hoangvu.ticketpeak.api.auth.controller;

import io.qzz.hoangvu.ticketpeak.api.auth.dto.LogoutRequest;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.LoginRequest;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.RefreshRequest;
import io.qzz.hoangvu.ticketpeak.api.auth.dto.TokenPairResponse;
import io.qzz.hoangvu.ticketpeak.api.auth.service.AuthService;
import io.qzz.hoangvu.ticketpeak.api.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenPairResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request), "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenPairResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(request), "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.message("Logged out"));
    }
}
