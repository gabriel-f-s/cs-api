package com.dev.cs_api.tenancy.company.controllers;

import com.dev.cs_api.tenancy.company.dtos.TenantBrandingResponse;
import com.dev.cs_api.tenancy.company.services.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenant/branding")
@Tag(name = "Tenancy - Branding", description = "Identidade visual do Tenant")
public class TenantBrandingController {

    private final TenantService service;

    public TenantBrandingController(TenantService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<TenantBrandingResponse> getMyBranding() {
        return ResponseEntity.ok(service.getMyBranding());
    }
}
