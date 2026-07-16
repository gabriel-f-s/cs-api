package com.dev.cs_api.core.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class SecurityUtils {

    private static Jwt getJwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        throw new IllegalStateException("Usuário não autenticado ou contexto de segurança vazio.");
    }

    public static UUID getUserId() {
        return UUID.fromString(getJwt().getSubject());
    }

    public static UUID getTenantId() {
        String tenantIdClaim = getJwt().getClaimAsString("tenantId");
        return tenantIdClaim != null ? UUID.fromString(tenantIdClaim) : null;
    }

    public static String getEmail() {
        return getJwt().getClaimAsString("email");
    }
}
