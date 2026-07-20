package com.dev.cs_api.identity.exceptions;

public class NoPermissionException extends RuntimeException {
    public NoPermissionException(String message) {
        super(message);
    }
}
