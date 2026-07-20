package com.dev.cs_api.identity.auth.services;

import com.dev.cs_api.identity.auth.exceptions.InvalidTokenException;
import com.dev.cs_api.identity.auth.models.RefreshToken;
import com.dev.cs_api.identity.user.models.User;
import com.dev.cs_api.identity.auth.repositories.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public RefreshToken findByToken(String token) {
        return repository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Refresh token inválido"));
    }

    public RefreshToken create(User user) {
        repository.deleteByUser(user);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        return repository.save(refreshToken);
    }

    public void deleteByToken(String token) {
        repository.deleteByToken(token);
    }

    public void delete(RefreshToken refreshToken) {
        repository.delete(refreshToken);
    }
}
