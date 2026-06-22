package com.dev.cs_api.identity.enums;

public enum AuthStatus {
    SUCCESS("SUCCESS"),
    MFA_REQUIRED("MFA_REQUIRED"),
    REFRESH_TOKEN("REFRESH_TOKEN"),
    FAILED("FAILED");

    private final String status;

    AuthStatus(String status) { this.status = status; }

    public String getStatus() { return status; }
}
