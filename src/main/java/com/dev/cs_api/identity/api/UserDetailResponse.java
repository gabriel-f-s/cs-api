package com.dev.cs_api.identity.api;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetailResponse(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID tenantId
) { }
