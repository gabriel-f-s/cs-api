package com.dev.cs_api.identity.user.repositories;

import com.dev.cs_api.identity.user.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
