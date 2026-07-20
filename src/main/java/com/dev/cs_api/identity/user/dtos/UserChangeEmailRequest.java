package com.dev.cs_api.identity.user.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserChangeEmailRequest(
        @Email
        @NotBlank(message = "O e-mail é obrigatório")
        @Size(max = 255)
        String email,
        @Email
        @NotBlank(message = "O e-mail de confirmação é obrigatório")
        @Size(max = 255)
        String confirmEmail
) {
}
