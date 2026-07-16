package com.dev.cs_api.identity.dtos.global;

import com.dev.cs_api.identity.models.User;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String name,
        String email
) {
    public MeResponse(User user) {
        this(user.getId(), user.getName(), user.getEmail());
    }
}
