package com.dev.cs_api.identity.controllers;

import com.dev.cs_api.identity.dtos.global.MeResponse;
import com.dev.cs_api.identity.dtos.global.ProfileResponse;
import com.dev.cs_api.identity.dtos.global.SecurityProfileResponse;
import com.dev.cs_api.identity.dtos.user.UserChangeEmailRequest;
import com.dev.cs_api.identity.dtos.user.UserChangePasswordRequest;
import com.dev.cs_api.identity.services.ProfileService;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@Tag(name = "Perfil", description = "Perfil e configurações do User/Admin")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Retorna informações básicas do usuário logado")
    @GetMapping("/me")
    public ResponseEntity<MeResponse> findMe() { return ResponseEntity.ok(profileService.findMe()); }

    @Operation(summary = "Retorna informações sobre o perfil do usuário logado")
    @GetMapping
    public ResponseEntity<ProfileResponse> findProfile() {
        return ResponseEntity.ok(profileService.findProfile());
    }

    @Operation(summary = "Retorna informações sobre o perfil com ênfase em segurança (MFA e Auditoria) do usuário logado")
    @GetMapping("/security")
    public ResponseEntity<SecurityProfileResponse> findSecurityProfile() { return ResponseEntity.ok(profileService.findSecurityProfile()); }

    @Operation(summary = "Permite o usuário logado atualizar seu nome e telefone")
    @PatchMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody JsonNode request) {
        return ResponseEntity.ok(profileService.updateProfile(request));
    }

    @Operation(summary = "Permite o usuário logado atualizar o seu email (apenas Admin)")
    @PatchMapping("/email")
    public ResponseEntity<ProfileResponse> changeEmail(@Valid @RequestBody UserChangeEmailRequest request) {
        return ResponseEntity.ok(profileService.changeEmail(request));
    }

    @Operation(summary = "Permite o usuário logado atualizar a sua senha")
    @PatchMapping("/security/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UserChangePasswordRequest request) {
        profileService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
