package com.dev.cs_api.identity.controllers;

import com.dev.cs_api.identity.dtos.admin.AdminDetailResponse;
import com.dev.cs_api.identity.dtos.admin.AdminUpdateRequest;
import com.dev.cs_api.identity.dtos.global.AdminCreateRequest;
import com.dev.cs_api.identity.dtos.global.AdminUserSummaryResponse;
import com.dev.cs_api.identity.services.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/admins")
@Tag(name = "Identity - Administradores do Sistema", description = "Gestão de Administradores do Sistema")
@PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "Retorna uma página contendo a quantidade solicitada de administradores")
    @GetMapping
    public ResponseEntity<Page<AdminUserSummaryResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(adminUserService.findAll(pageable));
    }

    @Operation(summary = "Retorna um administrador existente por ID")
    @GetMapping("/{id}")
    public ResponseEntity<AdminDetailResponse> findOne(@PathVariable UUID id) {
        return ResponseEntity.ok(adminUserService.findOne(id));
    }

    @Operation(summary = "Cria um administrador")
    @PostMapping
    public ResponseEntity<AdminDetailResponse> create(@RequestBody @Valid AdminCreateRequest request) {
        AdminDetailResponse response = adminUserService.create(request);
        return ResponseEntity.created(
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response)
                        .toUri()
        ).build();
    }

    @Operation(summary = "Atualiza parcialmente um administrador")
    @PatchMapping("/{id}")
    public ResponseEntity<AdminDetailResponse> update(@PathVariable UUID id, @RequestBody AdminUpdateRequest request) {
        return ResponseEntity.ok(adminUserService.update(id,request));
    }

    @Operation(summary = "Altera o status do administrador (ACTIVE ou DISABLED)")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        adminUserService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deleta um administrador")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        adminUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
