package com.dev.cs_api.identity.dtos.global;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateRequest(
        @NotBlank(message = "Nome é obrigatório")
        String name,
        @NotBlank(message = "E-mail é obrigatório")
        String email,
        @NotBlank(message = "Senha é obrigatório")
        @Size(min = 8, max = 64, message = "A senha deve ter entre 8 e 64 caracteres")
        String password,
        @NotBlank(message = "Telefone é obrigatório")
        String phoneNumber
) {
}
