package com.dev.cs_api.identity.api;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String name,
        String email,
        String role,
        String status,
        LocalDateTime createdAt,
        Boolean forcePasswordChange,
        UUID tenantId
) { }
