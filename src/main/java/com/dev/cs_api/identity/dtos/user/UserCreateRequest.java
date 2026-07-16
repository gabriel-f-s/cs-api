package com.dev.cs_api.identity.dtos.user;

import com.dev.cs_api.identity.enums.RoleName;

public record UserCreateRequest(
        String name,
        String email,
        String password,
        String phoneNumber,
        RoleName role
) {
}
