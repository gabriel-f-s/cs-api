package com.dev.cs_api.identity.enums;

import lombok.Getter;

@Getter
public enum AuthStatus {
    SUCCESS("SUCCESS"),
    MFA_REQUIRED("MFA_REQUIRED"),
    REFRESH_TOKEN("REFRESH_TOKEN"),
    FAILED("FAILED"),
    REQUIRE_PASSWORD_CHANGE("REQUIRE_PASSWORD_CHANGE");

    private final String status;

    AuthStatus(String status) { this.status = status; }

}
