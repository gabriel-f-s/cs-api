package com.dev.cs_api.identity.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminTenantUserApi {
    UserDetailResponse findOne(UUID userId);
    Page<UserSummaryResponse> findAll(Pageable pageable);
    Page<UserSummaryResponse> findAllByTenantId(UUID tenantId, Pageable pageable);
    void toggleStatus(UUID userId);
}
