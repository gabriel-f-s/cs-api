package com.dev.cs_api.orchestrator.admin_backoffice;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/admins/users")
@Tag(name = "Orchestrator (Identity x Tenancy) - Usuários", description = "Visualização de Usuários do Tenant através Administrador do Sistema")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AdminTenantOrchestratorController {

    private final AdminTenantOrchestratorService service;

    public AdminTenantOrchestratorController(AdminTenantOrchestratorService service) {
        this.service = service;
    }

    @Operation(summary = "Exibe todos os usuários de todos os Tenants")
    @GetMapping
    public ResponseEntity<Page<UserWithTenantSummaryResponse>> findAll(
            @RequestParam(required = false) UUID uuid,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(uuid, pageable));
    }

    @Operation(summary = "Exibe um usuário")
    @GetMapping("/{id}")
    public ResponseEntity<UserWithTenantDetailsResponse> findOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findOne(id));
    }

    @Operation(summary = "Altera o status de um usuário")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        service.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }
}
