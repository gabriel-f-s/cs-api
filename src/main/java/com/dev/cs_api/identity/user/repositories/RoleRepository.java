package com.dev.cs_api.identity.user.repositories;

import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
