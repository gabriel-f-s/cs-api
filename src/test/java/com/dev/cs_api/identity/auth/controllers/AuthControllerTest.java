package com.dev.cs_api.identity.auth.controllers;

import com.dev.cs_api.identity.auth.configs.SecurityConfig;
import com.dev.cs_api.identity.auth.dtos.*;
import com.dev.cs_api.identity.auth.enums.AuthStatus;
import com.dev.cs_api.identity.auth.services.AuthService;
import com.dev.cs_api.identity.auth.services.UserDetailsServiceImpl;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("POST /auth/login com body inválido deve retornar 400 Bad Request por validação @Valid")
    void shouldReturn400WhenLoginRequestInvalid() throws Exception {
        LoginRequest invalidRequest = new LoginRequest("", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/login com credenciais válidas deve retornar 200 OK")
    void shouldReturn200OnSuccessfulLogin() throws Exception {
        LoginRequest validRequest = new LoginRequest("user@test.com", "password123");
        AuthResponse response = new AuthResponse("access-token", "refresh-token", false, null, AuthStatus.SUCCESS);

        when(authService.login(any(LoginRequest.class), any(), any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /auth/refresh com refresh token em branco deve retornar 400 Bad Request")
    void shouldReturn400WhenRefreshTokenBlank() throws Exception {
        RefreshRequest invalidRequest = new RefreshRequest("");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/refresh com token válido deve retornar 200 OK")
    void shouldReturn200OnSuccessfulRefresh() throws Exception {
        RefreshRequest validRequest = new RefreshRequest("valid-refresh-token");
        AuthResponse response = new AuthResponse("new-access-token", "new-refresh-token", false, null, AuthStatus.REFRESH_TOKEN);

        when(authService.refresh(any(RefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    @DisplayName("POST /auth/logout com token válido deve retornar 200 OK")
    void shouldReturn200OnLogout() throws Exception {
        RefreshRequest validRequest = new RefreshRequest("valid-refresh-token");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        verify(authService).logout(any(RefreshRequest.class));
    }
}
