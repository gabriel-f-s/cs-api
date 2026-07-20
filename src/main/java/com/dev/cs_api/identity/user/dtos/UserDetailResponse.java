package com.dev.cs_api.identity.user.dtos;

import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.models.User;

import java.time.LocalDateTime;

public record UserDetailResponse(
        String id,
        String name,
        String email,
        String phoneNumber,
        RoleName role,
        UserStatus status,
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
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
