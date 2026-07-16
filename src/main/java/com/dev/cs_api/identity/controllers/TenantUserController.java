package com.dev.cs_api.identity.controllers;

import com.dev.cs_api.identity.dtos.global.ProfileResponse;
import com.dev.cs_api.identity.dtos.user.UserCreateRequest;
import com.dev.cs_api.identity.dtos.user.UserDetailResponse;
import com.dev.cs_api.identity.dtos.user.UserSummaryResponse;
import com.dev.cs_api.identity.dtos.user.UserUpdateRequest;
import com.dev.cs_api.identity.services.TenantUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários do Tenant", description = "CRUD de usuários do tenant")
public class TenantUserController {

    private final TenantUserService tenantUserService;

    public TenantUserController(TenantUserService tenantUserService) {
        this.tenantUserService = tenantUserService;
    }

    @Operation(summary = "Retorna um usuário caso exista por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<UserDetailResponse> findOne(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantUserService.findOne(id));
    }

    @Operation(summary = "Retorna todos os usuários existentes")
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Page<UserSummaryResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(tenantUserService.findAll(pageable));
    }

    @Operation(summary = "Cria um usuário")
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<UserSummaryResponse> create(@RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(tenantUserService.create(request));
    }

    @Operation(summary = "Atualiza em partes um usuário")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<UserSummaryResponse> update(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(tenantUserService.update(id, request));
    }

    @Operation(summary = "Altera o status do usuário")
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<UserDetailResponse> toggleStatus(UUID id) {
        return ResponseEntity.ok(tenantUserService.toggleStatus(id));
    }

    @Operation(summary = "Deleta um usuário")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tenantUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
