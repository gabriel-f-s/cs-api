package com.dev.cs_api.identity.user.controllers;

import com.dev.cs_api.identity.auth.configs.SecurityConfig;
import com.dev.cs_api.identity.auth.services.UserDetailsServiceImpl;
import com.dev.cs_api.identity.user.dtos.AdminCreateRequest;
import com.dev.cs_api.identity.user.dtos.AdminDetailResponse;
import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.services.AdminUserService;
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

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /admins sem token deve retornar 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/admins"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /admins sem ROLE_SYSTEM_ADMIN deve retornar 403 Forbidden")
    void shouldReturn403WhenForbiddenRole() throws Exception {
        mockMvc.perform(get("/admins")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admins com ROLE_SYSTEM_ADMIN deve retornar 200 OK")
    void shouldReturn200WhenSystemAdmin() throws Exception {
        when(adminUserService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/admins")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk());

        verify(adminUserService).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("POST /admins com JSON inválido deve retornar 400 Bad Request por causa da validação @Valid")
    void shouldReturn400WhenInvalidPayload() throws Exception {
        AdminCreateRequest invalidRequest = new AdminCreateRequest("", "email-invalido", "", "");

        mockMvc.perform(post("/admins")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admins com JSON válido deve retornar 201 Created")
    void shouldReturn201WhenValidPayload() throws Exception {
        AdminCreateRequest validRequest = new AdminCreateRequest("Admin Teste", "admin@teste.com", "senha123", "11999999999");
        UUID newId = UUID.randomUUID();
        AdminDetailResponse response = new AdminDetailResponse(
                newId, "Admin Teste", "admin@teste.com", "11999999999",
                RoleName.SYSTEM_ADMIN, UserStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "192.168.4.110", "Mozilla/5.0"
        );

        when(adminUserService.create(any(AdminCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/admins")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(newId.toString()))
                .andExpect(jsonPath("$.name").value("Admin Teste"));
    }

    @Test
    @DisplayName("PATCH /admins/{id}/toggle-status com ROLE_SYSTEM_ADMIN deve retornar 204 No Content")
    void shouldReturn204OnToggleStatus() throws Exception {
        UUID adminId = UUID.randomUUID();

        mockMvc.perform(patch("/admins/" + adminId + "/toggle-status")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNoContent());

        verify(adminUserService).toggleStatus(adminId);
    }
}
