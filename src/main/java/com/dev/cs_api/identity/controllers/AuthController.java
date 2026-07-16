package com.dev.cs_api.identity.controllers;

import com.dev.cs_api.identity.dtos.auth.*;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Autenticação do sistema")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Realiza login na aplicação")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        Map<String, String> userAgentAndIpAddress = getUserAgentAndIpAddress(servletRequest);
        return ResponseEntity.ok(authService.login(
                request,
                userAgentAndIpAddress.get("userAgent"),
                userAgentAndIpAddress.get("ipAddress")
        ));
    }

    @Operation(summary = "Atualiza o refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "Sai da aplicação")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Verifica o MFA e realiza o login")
    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthResponse> verifyMFAAndLogin(
            @Valid @RequestBody MfaVerifyRequest request,
            HttpServletRequest servletRequest
    ) {
        Map<String, String> userAgentAndIpAddress = getUserAgentAndIpAddress(servletRequest);
        return ResponseEntity.ok(authService.verifyMfaAndLogin(
                request,
                userAgentAndIpAddress.get("userAgent"),
                userAgentAndIpAddress.get("ipAddress")
        ));
    }

    @Operation(summary = "Atualiza a primeira senha de login")
    @PostMapping("/first-password")
    public ResponseEntity<AuthResponse> changeFirstPassword(
            @Valid @RequestBody FirstPasswordChangeRequest request,
            HttpServletRequest servletRequest
    ) {
        Map<String, String> userAgentAndIpAddress = getUserAgentAndIpAddress(servletRequest);
        return ResponseEntity.ok(authService.changeFirstPassword(
                request,
                userAgentAndIpAddress.get("userAgent"),
                userAgentAndIpAddress.get("ipAddress")
        ));
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
