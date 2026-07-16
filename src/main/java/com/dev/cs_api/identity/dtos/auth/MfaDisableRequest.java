package com.dev.cs_api.identity.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MfaDisableRequest(
        @NotBlank(message = "O código MFA é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "O código deve conter exatamente 6 números")
        String code
) {
}
