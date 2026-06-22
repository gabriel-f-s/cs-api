package com.dev.cs_api.identity.repositories;

import com.dev.cs_api.identity.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
