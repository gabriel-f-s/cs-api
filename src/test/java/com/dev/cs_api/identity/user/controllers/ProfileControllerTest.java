package com.dev.cs_api.identity.user.controllers;

import com.dev.cs_api.identity.auth.configs.SecurityConfig;
import com.dev.cs_api.identity.auth.services.UserDetailsServiceImpl;
import com.dev.cs_api.identity.user.dtos.MeResponse;
import com.dev.cs_api.identity.user.dtos.UserChangePasswordRequest;
import com.dev.cs_api.identity.user.services.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /profile/me sem token deve retornar 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/profile/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /profile/me com token válido deve retornar 200 OK")
    void shouldReturn200WhenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        MeResponse response = new MeResponse(userId, "Test User", "test@example.com");

        when(profileService.findMe()).thenReturn(response);

        mockMvc.perform(get("/profile/me")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("PATCH /profile/security/password com DTO inválido deve retornar 400 Bad Request por validação @Valid")
    void shouldReturn400WhenInvalidPasswordPayload() throws Exception {
        UserChangePasswordRequest invalid = new UserChangePasswordRequest("", "");

        mockMvc.perform(patch("/profile/security/password")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /profile/security/password com DTO válido deve retornar 204 No Content")
    void shouldReturn204WhenValidPasswordPayload() throws Exception {
        UserChangePasswordRequest valid = new UserChangePasswordRequest("NewPass123!", "NewPass123!");

        mockMvc.perform(patch("/profile/security/password")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid)))
                .andExpect(status().isNoContent());

        verify(profileService).changePassword(any(UserChangePasswordRequest.class));
    }
}
