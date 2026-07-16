package com.dev.cs_api.identity.dtos.user;

import java.util.UUID;

public record UserDeleteRequest(
        UUID id
) {
}
