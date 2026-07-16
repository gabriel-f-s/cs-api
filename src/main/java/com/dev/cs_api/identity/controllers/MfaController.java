package com.dev.cs_api.identity.controllers;

import com.dev.cs_api.identity.dtos.auth.MfaConfirmRequest;
import com.dev.cs_api.identity.dtos.auth.MfaDisableRequest;
import com.dev.cs_api.identity.dtos.auth.MfaSetupResponse;
import com.dev.cs_api.identity.services.MfaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profile/security/mfa")
@Tag(name = "MFA", description = "Configuração do MFA para o usuário")
public class MfaController {

    private final MfaService service;

    public MfaController(MfaService mfaService) {
        this.service = mfaService;
    }

    @Operation(summary = "Configura o MFA para o cadastro")
    @PostMapping("/setup")
    public ResponseEntity<MfaSetupResponse> setupMfa() {
        MfaSetupResponse response = service.generateSetup();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirma o cadastro do MFA")
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmMfa(@Valid @RequestBody MfaConfirmRequest request) {
        service.confirmSetup(request.code());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desativa o MFA")
    @PostMapping("/disable")
    public ResponseEntity<Void> disableMfa(@Valid @RequestBody MfaDisableRequest request) {
        service.disableMfa(request.code());
        return ResponseEntity.noContent().build();
    }
}
