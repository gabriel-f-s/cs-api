package com.dev.cs_api.orchestrator.admin_backoffice;

import com.dev.cs_api.identity.api.*;
import com.dev.cs_api.tenancy.api.TenantCompanyResponse;
import com.dev.cs_api.tenancy.api.TenantUserCompanyApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminTenantOrchestratorService {

    private final AdminTenantUserApi identityApi;
    private final TenantUserCompanyApi tenancyApi;

    public AdminTenantOrchestratorService(AdminTenantUserApi identityApi, TenantUserCompanyApi tenancyApi) {
        this.identityApi = identityApi;
        this.tenancyApi = tenancyApi;
    }

    public UserWithTenantDetailsResponse findOne(UUID id) {
        UserDetailResponse user = identityApi.findOne(id);
        TenantCompanyResponse tenant = tenancyApi.getTenantDetailsById(user.tenantId());
        return instanceUserWithTenantDetails(user, tenant);
    }

    public Page<UserWithTenantSummaryResponse> findAll(UUID tenantId, Pageable pageable) {
        Page<UserSummaryResponse> usersPage;

        if (tenantId != null) {
            usersPage = identityApi.findAllByTenantId(tenantId, pageable);
        } else {
            usersPage = identityApi.findAll(pageable);
        }

        Map<UUID, TenantCompanyResponse> tenantsMap = getTenancyDetailsFromUsersPage(usersPage);

        return usersPage.map(user -> {
            TenantCompanyResponse tenant = tenantsMap.getOrDefault(
                    user.tenantId(),
                    new TenantCompanyResponse(user.tenantId(), "N/A", "N/A", "N/A")
            );
            return instanceUserWithTenantSummary(user, tenant);
        });
    }

    public void toggleStatus(UUID userId) {
        identityApi.toggleStatus(userId);
    }

    private UserWithTenantDetailsResponse instanceUserWithTenantDetails(UserDetailResponse user, TenantCompanyResponse tenant) {
        return new UserWithTenantDetailsResponse(
                user.id(),
                user.name(),
                user.email(),
                user.phoneNumber(),
                user.role(),
                user.status(),
                user.createdAt(),
                user.updatedAt(),
                tenant.id(),
                tenant.tradeName(),
                tenant.corporateName(),
                tenant.document()
        );
    }

    private UserWithTenantSummaryResponse instanceUserWithTenantSummary(UserSummaryResponse user, TenantCompanyResponse tenant) {
        return new UserWithTenantSummaryResponse(
                user.id(),
                user.name(),
                user.email(),
                user.role(),
                user.status(),
                user.createdAt(),
                user.forcePasswordChange(),
                tenant.id(),
                tenant.tradeName(),
                tenant.document()
        );
    }

    private Map<UUID, TenantCompanyResponse> getTenancyDetailsFromUsersPage(Page<UserSummaryResponse> usersPage) {
        List<UUID> tenantIds = usersPage.getContent().stream()
                .map(UserSummaryResponse::tenantId)
                .distinct()
                .toList();

        return tenancyApi.getTenantDetails(tenantIds);
    }
}
