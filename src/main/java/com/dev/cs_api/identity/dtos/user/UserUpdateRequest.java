package com.dev.cs_api.identity.dtos.user;

public record UserUpdateRequest(
        String name,
        String email,
        String phoneNumber,
        String role,
        Boolean active
) {}

