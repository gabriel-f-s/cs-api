package com.dev.cs_api.orchestrator.admin_user_tenant;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserWithTenantDetailsResponse(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID tenantId,
        String tenantTradeName,
        String tenantCorporateName,
        String tenantDocument
) { }
