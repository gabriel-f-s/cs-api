package com.dev.cs_api.tenancy.services;

import com.dev.cs_api.core.security.SecurityUtils;
import com.dev.cs_api.tenancy.dtos.tenant.TenantBrandingResponse;
import com.dev.cs_api.tenancy.models.Tenant;
import com.dev.cs_api.tenancy.repositories.TenantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository repository;

    public TenantService(TenantRepository repository) {
        this.repository = repository;
    }

    public TenantBrandingResponse getMyBranding() {
        UUID tenantId = SecurityUtils.getTenantId();
        return new TenantBrandingResponse(findTenant(tenantId));
    }

    private Tenant findTenant(UUID tenantId) {
        return repository.findById(tenantId).orElseThrow(
                () -> new EntityNotFoundException("Tenant não encontrado")
        );
    }
}
