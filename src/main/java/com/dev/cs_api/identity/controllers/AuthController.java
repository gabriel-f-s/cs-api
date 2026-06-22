package com.dev.cs_api.identity.controllers;

import com.dev.cs_api.identity.dtos.AuthResponse;
import com.dev.cs_api.identity.dtos.LoginRequest;
import com.dev.cs_api.identity.dtos.MfaRequest;
import com.dev.cs_api.identity.dtos.RefreshRequest;
import com.dev.cs_api.identity.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Log in to the application")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        Map<String, String> userAgentAndIpAddress = getUserAgentAndIpAddress(servletRequest);
        return ResponseEntity.ok(authService.login(request, userAgentAndIpAddress.get("userAgent"), userAgentAndIpAddress.get("ipAddress")));
    }

    @Operation(summary = "Refresh the application token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "Log out from the application")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthResponse> verifyMFAAndLogin(
            @Valid @RequestBody MfaRequest request,
            HttpServletRequest servletRequest
    ) {
        Map<String, String> userAgentAndIpAddress = getUserAgentAndIpAddress(servletRequest);
        return ResponseEntity.ok(authService.verifyMfaAndLogin(request, userAgentAndIpAddress.get("userAgent"), userAgentAndIpAddress.get("ipAddress")));
    }

    private Map<String, String> getUserAgentAndIpAddress(HttpServletRequest servletRequest) {
        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = servletRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = servletRequest.getRemoteAddr();
        } else {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return new HashMap<>(Map.of("userAgent", userAgent, "ipAddress", ipAddress));
    }
}
