package com.dev.cs_api.tenancy.company.controllers;

import com.dev.cs_api.tenancy.company.dtos.TenantBrandingResponse;
import com.dev.cs_api.tenancy.company.services.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenant")
@Tag(name = "Tenancy - Tenants", description = "Gestão de Tenants do Sistema (Apenas SYSTEM_ADMIN)")
@PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
public class TenantController {

    private final TenantService service;

    public TenantController(TenantService service) {
        this.service = service;
    }

    @GetMapping("/me/branding")
    public ResponseEntity<TenantBrandingResponse> getMyBranding() {
        return ResponseEntity.ok(service.getMyBranding());
    }
}
