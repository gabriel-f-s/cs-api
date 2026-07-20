package com.dev.cs_api.identity.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaVerifyRequest(
        @NotBlank(message = "O token MFA é obrigatório")
        String token,
        @NotBlank(message = "O código é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "O código deve conter exatamente 6 números")
        String code
) {
}
