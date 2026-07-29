package com.dev.cs_api.orchestrator.admin_user_tenant;

import com.dev.cs_api.identity.auth.configs.SecurityConfig;
import com.dev.cs_api.identity.auth.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTenantOrchestratorController.class)
@Import(SecurityConfig.class)
class AdminTenantOrchestratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminTenantOrchestratorService service;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("GET /admins/users sem token deve retornar 401 Unauthorized")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/admins/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /admins/users sem ROLE_SYSTEM_ADMIN deve retornar 403 Forbidden")
    void shouldReturn403WhenNotSystemAdmin() throws Exception {
        mockMvc.perform(get("/admins/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERATOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admins/users com ROLE_SYSTEM_ADMIN deve retornar 200 OK")
    void shouldReturn200WhenSystemAdmin() throws Exception {
        when(service.findAll(eq(null), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/admins/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isOk());

        verify(service).findAll(eq(null), any(Pageable.class));
    }

    @Test
    @DisplayName("PATCH /admins/users/{id}/toggle-status com ROLE_SYSTEM_ADMIN deve retornar 204 No Content")
    void shouldReturn204OnToggleStatus() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(patch("/admins/users/" + userId + "/toggle-status")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                .andExpect(status().isNoContent());

        verify(service).toggleStatus(userId);
    }
}
