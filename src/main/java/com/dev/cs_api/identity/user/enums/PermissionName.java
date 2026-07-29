package com.dev.cs_api.identity.user.enums;

public enum PermissionName {
    ADMIN_CREATE("admin:create"),
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),

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
