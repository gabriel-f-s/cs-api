package com.dev.cs_api.identity.user.dtos;

import com.dev.cs_api.identity.user.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "Nome é obrigatório")
        String name,
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "Email não é válido")
        String email,
        @NotBlank(message = "Senha é obrigatório")
        @Size(min = 8, max = 64, message = "A senha deve ter entre 8 e 64 caracteres")
        String password,
        @NotBlank(message = "Telefone é obrigatório")
        String phoneNumber,
        @NotNull(message = "Role é obrigatório")
        RoleName role
) {
}
