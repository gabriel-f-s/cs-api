package com.dev.cs_api.identity.auth.repositories;

import com.dev.cs_api.identity.auth.models.RefreshToken;
import com.dev.cs_api.identity.user.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    void deleteByUser(User user);
    void deleteByToken(String token);

    Optional<RefreshToken> findByToken(String token);
}
