package com.dev.cs_api.identity.user.dtos;

public record AdminUpdateRequest(
        String name,
        String email,
        String phoneNumber,
        Boolean active
) {}
