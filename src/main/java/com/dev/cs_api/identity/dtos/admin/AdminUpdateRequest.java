package com.dev.cs_api.identity.dtos.admin;

public record AdminUpdateRequest(
        String name,
        String email,
        String phoneNumber,
        Boolean active
) {}
