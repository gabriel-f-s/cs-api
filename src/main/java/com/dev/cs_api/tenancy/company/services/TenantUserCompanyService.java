package com.dev.cs_api.tenancy.company.services;

import com.dev.cs_api.tenancy.api.TenantCompanyResponse;
import com.dev.cs_api.tenancy.api.TenantUserCompanyApi;
import com.dev.cs_api.tenancy.company.models.Tenant;
import com.dev.cs_api.tenancy.company.repositories.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TenantUserCompanyService implements TenantUserCompanyApi {

    private final TenantRepository repository;

    public TenantUserCompanyService(TenantRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public TenantCompanyResponse getTenantDetailsById(UUID id) {
        Tenant tenant = repository.findById(id).orElseThrow();
        return new TenantCompanyResponse(
                tenant.getId(),
                tenant.getTradeName(),
                tenant.getCorporateName(),
                tenant.getDocument()
        );
    }

    @Override
    public Map<UUID, TenantCompanyResponse> getTenantDetails(List<UUID> ids) {
        Map<UUID, TenantCompanyResponse> map = new HashMap<>();
        repository.findAllById(ids).forEach(tenant -> {
            map.put(tenant.getId(), new TenantCompanyResponse(
                    tenant.getId(),
                    tenant.getTradeName(),
                    tenant.getCorporateName(),
                    tenant.getDocument()
            ));
        });
        return map;
    }
}
