package com.dev.cs_api.identity.dtos.auth;

public record MfaSetupResponse(
        String token,
        String otpAuthUri
) {
}
