package com.dev.cs_api.orchestrator.admin_backoffice;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserWithTenantSummaryResponse(
        UUID id,
        String name,
        String email,
        String role,
        String status,
        LocalDateTime createdAt,
        Boolean forcePasswordChange,
        UUID tenantId,
        String tenantTradeName,
        String tenantDocument
) {
}
