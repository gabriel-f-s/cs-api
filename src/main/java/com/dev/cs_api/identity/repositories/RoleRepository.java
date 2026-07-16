package com.dev.cs_api.identity.repositories;

import com.dev.cs_api.identity.enums.RoleName;
import com.dev.cs_api.identity.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
