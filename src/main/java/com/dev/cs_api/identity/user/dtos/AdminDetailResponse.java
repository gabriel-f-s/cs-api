package com.dev.cs_api.identity.user.dtos;

import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.models.Admin;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminDetailResponse(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        RoleName role,
        UserStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        String lastLoginIP,
        String lastLoginUserAgent
) {
    public AdminDetailResponse(Admin admin) {
        this(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                admin.getPhoneNumber(),
                admin.getRole().getName(),
                admin.getStatus(),
                admin.getCreatedAt(),
                admin.getUpdatedAt(),
                admin.getLastLoginAt(),
                admin.getLastLoginIP(),
                admin.getLastLoginUserAgent()
        );
    }
}
