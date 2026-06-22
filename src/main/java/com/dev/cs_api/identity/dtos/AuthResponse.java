package com.dev.cs_api.identity.dtos;

import com.dev.cs_api.identity.enums.AuthStatus;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        boolean mfaRequired,
        String mfaToken,
        AuthStatus status
) {
}
