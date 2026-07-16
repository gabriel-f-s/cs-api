package com.dev.cs_api.identity.dtos.user;

import com.dev.cs_api.identity.dtos.global.SecurityProfileResponse;
import com.dev.cs_api.identity.models.User;

public record UserSecurityProfileResponse(
        Boolean mfaEnabled
) implements SecurityProfileResponse {
    public UserSecurityProfileResponse(User user) {
        this(user.getMfaEnabled());
    }
}
