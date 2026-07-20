package com.dev.cs_api.identity.user.dtos.user;

import com.dev.cs_api.identity.user.dtos.global.SecurityProfileResponse;
import com.dev.cs_api.identity.user.models.User;

public record UserSecurityProfileResponse(
        Boolean mfaEnabled
) implements SecurityProfileResponse {
    public UserSecurityProfileResponse(User user) {
        this(user.getMfaEnabled());
    }
}
