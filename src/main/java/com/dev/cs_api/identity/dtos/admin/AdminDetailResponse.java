package com.dev.cs_api.identity.dtos.admin;

import com.dev.cs_api.identity.models.Admin;

import java.time.LocalDateTime;

public record AdminDetailResponse(
        String id,
        String name,
        String email,
        String phoneNumber,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastLoginAt,
        String lastLoginIP,
        String lastLoginUserAgent
) {
    public AdminDetailResponse(Admin admin) {
        this(
                admin.getId().toString(),
                admin.getName(),
                admin.getEmail(),
                admin.getPhoneNumber(),
                admin.getRole().getName().toString(),
                admin.getStatus().toString(),
                admin.getCreatedAt(),
                admin.getUpdatedAt(),
                admin.getLastLoginAt(),
                admin.getLastLoginIP(),
                admin.getLastLoginUserAgent()
        );
    }
}
