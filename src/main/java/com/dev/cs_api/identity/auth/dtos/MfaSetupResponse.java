package com.dev.cs_api.identity.auth.dtos;

public record MfaSetupResponse(
        String token,
        String otpAuthUri
) {
}
