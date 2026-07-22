package com.dev.cs_api.identity.user.services;

import com.dev.cs_api.identity.api.*;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.models.User;

import com.dev.cs_api.identity.user.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AdminTenantUserService implements AdminTenantUserApi {

    private final UserRepository userRepository;

    public AdminTenantUserService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Override
    public UserDetailResponse findOne(UUID userId) {
        return instanceUserDetailResponse(findUser(userId));
    }

    @Override
    public Page<UserSummaryResponse> findAll(Pageable pageable) {
        return userRepository.findAllWithTenantId(pageable).map(this::instanceUserSummaryResponse);
    }

    @Override
    public Page<UserSummaryResponse> findAllByTenantId(UUID tenantId, Pageable pageable) {
        return userRepository.findAllByTenantId(tenantId, pageable).map(this::instanceUserSummaryResponse);
    }

    @Override
    @Transactional
    public void toggleStatus(UUID userId) {
        User user = findUser(userId);
        if (user.getStatus() == UserStatus.ACTIVE ) {
            user.setStatus(UserStatus.DISABLED);
        } else  { user.setStatus(UserStatus.ACTIVE); }
        userRepository.save(user);
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UsernameNotFoundException("Usuário não encontrado")
        );
    }

    private UserDetailResponse instanceUserDetailResponse(User user) {
        return new UserDetailResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().getName().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getTenantId()
        );
    }

    private UserSummaryResponse instanceUserSummaryResponse(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getForcePasswordChange(),
                user.getTenantId()
        );
    }
}
