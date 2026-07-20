package com.dev.cs_api.identity.auth.dtos;

import com.dev.cs_api.identity.auth.enums.AuthStatus;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean mfaRequired,
        String mfaToken,
        AuthStatus status
) {
}
