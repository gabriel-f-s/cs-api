package com.dev.cs_api.identity.user.controllers;

import com.dev.cs_api.identity.user.dtos.user.UserCreateRequest;
import com.dev.cs_api.identity.user.dtos.user.UserDetailResponse;
import com.dev.cs_api.identity.user.dtos.global.AdminUserSummaryResponse;
import com.dev.cs_api.identity.user.dtos.user.UserUpdateRequest;
import com.dev.cs_api.identity.user.services.TenantUserService;
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
@RequestMapping("/users")
@Tag(name = "Identity - Usuários do Tenant", description = "Gestão de Usuários do Tenant")
public class TenantUserController {

    private final TenantUserService tenantUserService;

    public TenantUserController(TenantUserService tenantUserService) {
        this.tenantUserService = tenantUserService;
    }

    @Operation(summary = "Retorna uma página contendo a quantidade solicitada de usuários")
    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<Page<AdminUserSummaryResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(tenantUserService.findAll(pageable));
    }

    @Operation(summary = "Retorna um usuário existente por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<UserDetailResponse> findOne(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantUserService.findOne(id));
    }

    @Operation(summary = "Cria um usuário")
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ResponseEntity<UserDetailResponse> create(@RequestBody @Valid UserCreateRequest request) {
        UserDetailResponse response = tenantUserService.create(request);
        return ResponseEntity.created(
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response)
                        .toUri()
        ).build();
    }

    @Operation(summary = "Atualiza parcialmente um usuário")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<UserDetailResponse> update(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(tenantUserService.update(id, request));
    }

    @Operation(summary = "Altera o status do usuário (ACTIVE ou DISABLED)")
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('user:update')")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        tenantUserService.toggleStatus(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Deleta um usuário")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tenantUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
