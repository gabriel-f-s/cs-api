package com.dev.cs_api.identity.dtos.user;

import com.dev.cs_api.identity.enums.RoleName;
import com.dev.cs_api.identity.models.User;

import java.time.LocalDateTime;

public record UserDetailResponse(
        String id,
        String name,
        String email,
        String phoneNumber,
        RoleName role,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public UserDetailResponse(User user) {
        this(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().getName(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
