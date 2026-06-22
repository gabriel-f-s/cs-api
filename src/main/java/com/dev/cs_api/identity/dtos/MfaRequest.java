package com.dev.cs_api.identity.dtos;

public record MfaRequest(
        String token,
        int code
) {
}
