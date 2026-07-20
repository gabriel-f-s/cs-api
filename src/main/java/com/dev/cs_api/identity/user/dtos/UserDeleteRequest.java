package com.dev.cs_api.identity.user.dtos;

import java.util.UUID;

public record UserDeleteRequest(
        UUID id
) {
}
