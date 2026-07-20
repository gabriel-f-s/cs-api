package com.dev.cs_api.identity.user.dtos.global;

import com.dev.cs_api.identity.user.models.Admin;
import com.dev.cs_api.identity.user.models.User;

public record AdminUserSummaryResponse(
        String id,
        String name,
        String email,
        String role,
        String status,
        String createdAt,
        Boolean forcePasswordChange
) {
    public AdminUserSummaryResponse(User user) {
        this(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName().toString(),
                user.getStatus().toString(),
                user.getCreatedAt().toString(),
                user.getForcePasswordChange()
        );
    }

    public AdminUserSummaryResponse(Admin admin) {
        this(
                admin.getId().toString(),
                admin.getName(),
                admin.getEmail(),
                admin.getRole().getName().toString(),
                admin.getStatus().toString(),
                admin.getCreatedAt().toString(),
                admin.getForcePasswordChange()
        );
    }
}
