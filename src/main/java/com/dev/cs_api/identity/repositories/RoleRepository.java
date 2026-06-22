package com.dev.cs_api.identity.repositories;

import com.dev.cs_api.identity.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
