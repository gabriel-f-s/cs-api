package com.dev.cs_api.tenancy.controllers;

import com.dev.cs_api.tenancy.dtos.tenant.TenantBrandingResponse;
import com.dev.cs_api.tenancy.services.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenant")
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
