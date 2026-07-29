package com.dev.cs_api.identity.user.controllers;

import com.dev.cs_api.identity.auth.configs.SecurityConfig;
import com.dev.cs_api.identity.auth.services.UserDetailsServiceImpl;
import com.dev.cs_api.identity.user.dtos.UserCreateRequest;
import com.dev.cs_api.identity.user.dtos.UserDetailResponse;
import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.services.TenantUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TenantUserController.class)
@Import(SecurityConfig.class)
class TenantUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TenantUserService tenantUserService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /users sem token deve retornar 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users sem authority user:read deve retornar 403 Forbidden")
    void shouldReturn403WhenMissingReadAuthority() throws Exception {
        mockMvc.perform(get("/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("other:authority"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /users com authority user:read deve retornar 200 OK")
    void shouldReturn200WhenHasReadAuthority() throws Exception {
        when(tenantUserService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("user:read"))))
                .andExpect(status().isOk());

        verify(tenantUserService).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("POST /users com DTO inválido deve retornar 400 Bad Request por validação @Valid")
    void shouldReturn400WhenInvalidPayload() throws Exception {
        UserCreateRequest invalidRequest = new UserCreateRequest("", "email-invalido", "", "", null);

        mockMvc.perform(post("/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("user:create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users com autorização e DTO válido deve retornar 201 Created")
    void shouldReturn201WhenValidPayload() throws Exception {
        UserCreateRequest validRequest = new UserCreateRequest("Novo Usuario", "user@test.com", "senha1234!", "11988888888", RoleName.OPERATOR);
        UUID newId = UUID.randomUUID();
        UserDetailResponse response = new UserDetailResponse(
                newId, "Novo Usuario", "user@test.com", "11988888888",
                RoleName.OPERATOR, UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now()
        );

        when(tenantUserService.create(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("user:create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(newId.toString()))
                .andExpect(jsonPath("$.name").value("Novo Usuario"));
    }

    @Test
    @DisplayName("DELETE /users/{id} com authority user:delete deve retornar 204 No Content")
    void shouldReturn204OnDelete() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/users/" + userId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("user:delete"))))
                .andExpect(status().isNoContent());

        verify(tenantUserService).delete(userId);
    }
}
