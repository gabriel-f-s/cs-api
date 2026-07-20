package com.dev.cs_api.identity.user.dtos;

import com.dev.cs_api.identity.user.models.Admin;

import java.time.LocalDateTime;

public record AdminSecurityProfileResponse(
        Boolean mfaEnabled,
        LocalDateTime lastLoginAt,
        String lastLoginIP,
        String lastLoginUserAgent
) implements SecurityProfileResponse {
    public AdminSecurityProfileResponse(Admin admin) {
        this(
                admin.getMfaEnabled(),
                admin.getLastLoginAt(),
                admin.getLastLoginIP(),
                admin.getLastLoginUserAgent()
        );
    }
}
