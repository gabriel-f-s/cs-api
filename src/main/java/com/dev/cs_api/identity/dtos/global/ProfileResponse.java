package com.dev.cs_api.identity.dtos.global;

import com.dev.cs_api.identity.enums.RoleName;
import com.dev.cs_api.identity.models.Admin;
import com.dev.cs_api.identity.models.User;

import java.time.LocalDateTime;

public record ProfileResponse(
        String id,
        String name,
        String email,
        String phoneNumber,
        RoleName role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public ProfileResponse(User user) {
        this(
                user.getId().toString(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole().getName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
