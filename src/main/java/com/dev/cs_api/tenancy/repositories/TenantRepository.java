package com.dev.cs_api.tenancy.repositories;

import com.dev.cs_api.tenancy.models.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
