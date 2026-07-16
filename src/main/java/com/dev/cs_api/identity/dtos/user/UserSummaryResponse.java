package com.dev.cs_api.identity.dtos.user;

import com.dev.cs_api.identity.enums.UserStatus;
import com.dev.cs_api.identity.models.User;

public record UserSummaryResponse (
        String id,
        String name,
        String email,
        String role,
        UserStatus status,
        String createdAt,
        Boolean forcePasswordChange
) {
    public UserSummaryResponse(User user) {
        this(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName().toString(),
                user.getStatus(),
                user.getCreatedAt().toString(),
                user.getForcePasswordChange()
        );
    }
}
