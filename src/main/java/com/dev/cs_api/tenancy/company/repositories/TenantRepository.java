package com.dev.cs_api.tenancy.company.repositories;

import com.dev.cs_api.tenancy.company.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
