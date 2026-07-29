package com.dev.cs_api.identity.user.services;

import com.dev.cs_api.identity.user.dtos.UserCreateRequest;
import com.dev.cs_api.identity.user.dtos.UserDetailResponse;
import com.dev.cs_api.identity.user.dtos.AdminUserSummaryResponse;
import com.dev.cs_api.identity.user.dtos.UserUpdateRequest;
import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.exceptions.NoPermissionException;
import com.dev.cs_api.identity.user.models.Role;
import com.dev.cs_api.identity.user.models.User;
import com.dev.cs_api.identity.user.repositories.RoleRepository;
import com.dev.cs_api.identity.user.repositories.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TenantUserService tenantUserService;

    private UUID tenantId;
    private UUID loggedUserId;
    private UUID targetUserId;
    private User loggedUser;
    private User targetUser;
    private Role tenantAdminRole;
    private Role managerRole;
    private Role userRole;
    private Role systemAdminRole;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        loggedUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();

        tenantAdminRole = new Role();
        tenantAdminRole.setId(1L);
        tenantAdminRole.setName(RoleName.TENANT_ADMIN);

        managerRole = new Role();
        managerRole.setId(2L);
        managerRole.setName(RoleName.MANAGER);

        userRole = new Role();
        userRole.setId(3L);
        userRole.setName(RoleName.OPERATOR);

        systemAdminRole = new Role();
        systemAdminRole.setId(4L);
        systemAdminRole.setName(RoleName.SYSTEM_ADMIN);

        loggedUser = new User();
        loggedUser.setId(loggedUserId);
        loggedUser.setName("Logged User");
        loggedUser.setEmail("logged@test.com");
        loggedUser.setRole(tenantAdminRole);
        loggedUser.setTenantId(tenantId);
        loggedUser.setStatus(UserStatus.ACTIVE);
        loggedUser.setCreatedAt(LocalDateTime.now());
        loggedUser.setUpdatedAt(LocalDateTime.now());

        targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setName("Target User");
        targetUser.setEmail("target@test.com");
        targetUser.setRole(userRole);
        targetUser.setTenantId(tenantId);
        targetUser.setStatus(UserStatus.ACTIVE);
        targetUser.setCreatedAt(LocalDateTime.now());
        targetUser.setUpdatedAt(LocalDateTime.now());

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", loggedUserId.toString())
                .claim("tenantId", tenantId.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("findOne")
    class FindOne {

        @Test
        @DisplayName("Deve retornar UserDetailResponse quando o usuário for encontrado no tenant")
        void shouldReturnUserWhenFound() {
            when(userRepository.findByIdAndTenantId(targetUserId, tenantId)).thenReturn(Optional.of(targetUser));

            UserDetailResponse response = tenantUserService.findOne(targetUserId);

            assertNotNull(response);
            assertEquals(targetUserId, response.id());
            assertEquals("Target User", response.name());
            verify(userRepository).findByIdAndTenantId(targetUserId, tenantId);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar uma página com os usuários do tenant")
        void shouldReturnPageOfUsers() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> usersPage = new PageImpl<>(List.of(targetUser));

            when(userRepository.findAllByTenantId(tenantId, pageable)).thenReturn(usersPage);

            Page<AdminUserSummaryResponse> result = tenantUserService.findAll(pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Target User", result.getContent().getFirst().name());
            verify(userRepository).findAllByTenantId(tenantId, pageable);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar usuário com sucesso")
        void shouldCreateUserSuccessfully() {
            UserCreateRequest request = new UserCreateRequest("Novo User", "novo@test.com", "pass123", "11988888888", RoleName.OPERATOR);

            when(roleRepository.findByName(RoleName.OPERATOR)).thenReturn(Optional.of(userRole));
            when(userRepository.findByIdAndTenantId(loggedUserId, tenantId)).thenReturn(Optional.of(loggedUser));
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPass");

            UserDetailResponse response = tenantUserService.create(request);

            assertNotNull(response);
            assertEquals("Novo User", response.name());
            assertEquals("novo@test.com", response.email());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException quando o e-mail já existir")
        void shouldThrowExceptionWhenEmailExists() {
            UserCreateRequest request = new UserCreateRequest("Novo User", "existente@test.com", "pass123","11988888888", RoleName.OPERATOR);

            when(roleRepository.findByName(RoleName.OPERATOR)).thenReturn(Optional.of(userRole));
            when(userRepository.findByIdAndTenantId(loggedUserId, tenantId)).thenReturn(Optional.of(loggedUser));
            when(userRepository.findByEmail("existente@test.com")).thenReturn(Optional.of(targetUser));

            assertThrows(EntityExistsException.class, () -> tenantUserService.create(request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar NoPermissionException se tentar criar SYSTEM_ADMIN")
        void shouldThrowExceptionWhenCreatingSystemAdmin() {
            UserCreateRequest request = new UserCreateRequest("Admin User", "sysadmin@test.com", "pass123","11988888888", RoleName.SYSTEM_ADMIN);

            when(roleRepository.findByName(RoleName.SYSTEM_ADMIN)).thenReturn(Optional.of(systemAdminRole));
            when(userRepository.findByIdAndTenantId(loggedUserId, tenantId)).thenReturn(Optional.of(loggedUser));

            assertThrows(NoPermissionException.class, () -> tenantUserService.create(request));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar NoPermissionException se MANAGER tentar criar TENANT_ADMIN")
        void shouldThrowExceptionWhenManagerCreatesTenantAdmin() {
            loggedUser.setRole(managerRole);
            UserCreateRequest request = new UserCreateRequest("Novo Admin", "admin@test.com", "pass123","11988888888", RoleName.TENANT_ADMIN);

            when(roleRepository.findByName(RoleName.TENANT_ADMIN)).thenReturn(Optional.of(tenantAdminRole));
            when(userRepository.findByIdAndTenantId(loggedUserId, tenantId)).thenReturn(Optional.of(loggedUser));
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            assertThrows(NoPermissionException.class, () -> tenantUserService.create(request));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar os dados do usuário com sucesso")
        void shouldUpdateUserSuccessfully() {
            UserUpdateRequest request = new UserUpdateRequest("Nome Alterado", "novoemail@test.com", "11977777777", "OPERATOR", true);

            when(userRepository.findByIdAndTenantId(targetUserId, tenantId)).thenReturn(Optional.of(targetUser));
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            when(roleRepository.findByName(RoleName.OPERATOR)).thenReturn(Optional.of(userRole));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserDetailResponse response = tenantUserService.update(targetUserId, request);

            assertNotNull(response);
            assertEquals("Nome Alterado", response.name());
            assertEquals("novoemail@test.com", response.email());
            verify(userRepository).save(targetUser);
        }

        @Test
        @DisplayName("Deve lançar NoPermissionException se tentar alterar role para SYSTEM_ADMIN")
        void shouldThrowExceptionWhenUpdatingToSystemAdmin() {
            UserUpdateRequest request = new UserUpdateRequest(null, null, null, "SYSTEM_ADMIN", null);

            when(userRepository.findByIdAndTenantId(targetUserId, tenantId)).thenReturn(Optional.of(targetUser));
            when(roleRepository.findByName(RoleName.SYSTEM_ADMIN)).thenReturn(Optional.of(systemAdminRole));

            assertThrows(NoPermissionException.class, () -> tenantUserService.update(targetUserId, request));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("toggleStatus")
    class ToggleStatus {

        @Test
        @DisplayName("Deve alterar status de ACTIVE para DISABLED")
        void shouldToggleActiveToDisabled() {
            targetUser.setStatus(UserStatus.ACTIVE);
            when(userRepository.findByIdAndTenantId(targetUserId, tenantId)).thenReturn(Optional.of(targetUser));

            tenantUserService.toggleStatus(targetUserId);

            assertEquals(UserStatus.DISABLED, targetUser.getStatus());
            verify(userRepository).save(targetUser);
        }

        @Test
        @DisplayName("Deve alterar status de DISABLED para ACTIVE")
        void shouldToggleDisabledToActive() {
            targetUser.setStatus(UserStatus.DISABLED);
            when(userRepository.findByIdAndTenantId(targetUserId, tenantId)).thenReturn(Optional.of(targetUser));

            tenantUserService.toggleStatus(targetUserId);

            assertEquals(UserStatus.ACTIVE, targetUser.getStatus());
            verify(userRepository).save(targetUser);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve deletar usuário por userId e tenantId")
        void shouldDeleteUser() {
            tenantUserService.delete(targetUserId);

            verify(userRepository).deleteByIdAndTenantId(targetUserId, tenantId);
        }
    }
}
