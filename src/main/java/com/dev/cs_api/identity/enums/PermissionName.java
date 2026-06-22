package com.dev.cs_api.identity.enums;

public enum PermissionName {
    TENANT_CREATE("tenant:create"),
    TENANT_READ("tenant:read"),
    TENANT_UPDATE("tenant:update"),
    TENANT_DELETE("tenant:delete"),
    USER_CREATE("user:create"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete");

    private final String permission;

    PermissionName(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
