package com.dev.cs_api.tenancy.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TenantUserCompanyApi {
    boolean existsById(UUID id);
    TenantCompanyResponse getTenantDetailsById(UUID id);
    Map<UUID, TenantCompanyResponse> getTenantDetails(List<UUID> ids);
}
