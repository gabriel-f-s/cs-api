package com.dev.cs_api.identity.user.repositories;

import com.dev.cs_api.identity.user.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByIdAndTenantId(UUID id, UUID tenantId);
    Page<User> findAllByTenantId(UUID tenantId, Pageable pageable);

    void deleteByIdAndTenantId(UUID userId,  UUID tenantId);
}
