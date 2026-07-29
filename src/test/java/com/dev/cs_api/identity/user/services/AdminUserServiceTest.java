package com.dev.cs_api.identity.user.services;

import com.dev.cs_api.identity.user.dtos.AdminCreateRequest;
import com.dev.cs_api.identity.user.dtos.AdminDetailResponse;
import com.dev.cs_api.identity.user.dtos.AdminUpdateRequest;
import com.dev.cs_api.identity.user.dtos.AdminUserSummaryResponse;
import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.models.Admin;
import com.dev.cs_api.identity.user.models.Role;
import com.dev.cs_api.identity.user.repositories.AdminRepository;
import com.dev.cs_api.identity.user.repositories.RoleRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    private Admin admin;
    private Role systemAdminRole;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        systemAdminRole = new Role();
        systemAdminRole.setId(1L);
        systemAdminRole.setName(RoleName.SYSTEM_ADMIN);

        admin = new Admin();
        admin.setId(adminId);
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setPassword("encodedPassword");
        admin.setPhoneNumber("11999999999");
        admin.setRole(systemAdminRole);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("findOne")
    class FindOne {

        @Test
        @DisplayName("Deve retornar AdminDetailResponse quando o admin for encontrado")
        void shouldReturnAdminDetailResponseWhenFound() {
            when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

            AdminDetailResponse response = adminUserService.findOne(adminId);

            assertNotNull(response);
            assertEquals(adminId, response.id());
            assertEquals("Admin User", response.name());
            assertEquals("admin@test.com", response.email());
            verify(adminRepository).findById(adminId);
        }

        @Test
        @DisplayName("Deve lançar UsernameNotFoundException quando o admin não for encontrado")
        void shouldThrowExceptionWhenNotFound() {
            when(adminRepository.findById(adminId)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> adminUserService.findOne(adminId));
            verify(adminRepository).findById(adminId);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar uma página de AdminUserSummaryResponse")
        void shouldReturnPageOfAdminSummary() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Admin> adminPage = new PageImpl<>(List.of(admin));

            when(adminRepository.findAll(pageable)).thenReturn(adminPage);

            Page<AdminUserSummaryResponse> result = adminUserService.findAll(pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Admin User", result.getContent().getFirst().name());
            verify(adminRepository).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar um admin com sucesso")
        void shouldCreateAdminSuccessfully() {
            AdminCreateRequest request = new AdminCreateRequest("Novo Admin", "novo@test.com", "pass123", "11988888888");

            when(roleRepository.findByName(RoleName.SYSTEM_ADMIN)).thenReturn(Optional.of(systemAdminRole));
            when(adminRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
            when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> {
                Admin saved = invocation.getArgument(0);
                saved.setId(UUID.randomUUID());
                saved.setCreatedAt(LocalDateTime.now());
                saved.setUpdatedAt(LocalDateTime.now());
                return saved;
            });

            AdminDetailResponse response = adminUserService.create(request);

            assertNotNull(response);
            assertEquals("Novo Admin", response.name());
            assertEquals("novo@test.com", response.email());
            verify(roleRepository).findByName(RoleName.SYSTEM_ADMIN);
            verify(adminRepository).findByEmail(request.email());
            verify(passwordEncoder).encode(request.password());
            verify(adminRepository).save(any(Admin.class));
        }

        @Test
        @DisplayName("Deve lançar EntityNotFoundException quando a role SYSTEM_ADMIN não for encontrada")
        void shouldThrowExceptionWhenRoleNotFound() {
            AdminCreateRequest request = new AdminCreateRequest("Novo Admin", "novo@test.com", "pass123", "11988888888");

            when(roleRepository.findByName(RoleName.SYSTEM_ADMIN)).thenReturn(Optional.empty());

            assertThrows(EntityNotFoundException.class, () -> adminUserService.create(request));
            verify(adminRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException quando o e-mail já existir")
        void shouldThrowExceptionWhenEmailExists() {
            AdminCreateRequest request = new AdminCreateRequest("Novo Admin", "admin@test.com", "pass123", "11988888888");

            when(roleRepository.findByName(RoleName.SYSTEM_ADMIN)).thenReturn(Optional.of(systemAdminRole));
            when(adminRepository.findByEmail(request.email())).thenReturn(Optional.of(admin));

            assertThrows(EntityExistsException.class, () -> adminUserService.create(request));
            verify(adminRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar os dados do admin com sucesso")
        void shouldUpdateAdminSuccessfully() {
            AdminUpdateRequest request = new AdminUpdateRequest("Nome Atualizado", "novoemail@test.com", "11977777777", false);

            when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(adminRepository.findByEmail(request.email())).thenReturn(Optional.empty());
            when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AdminDetailResponse response = adminUserService.update(adminId, request);

            assertNotNull(response);
            assertEquals("Nome Atualizado", response.name());
            assertEquals("novoemail@test.com", response.email());
            assertEquals(UserStatus.DISABLED, admin.getStatus());
            verify(adminRepository).save(admin);
        }

        @Test
        @DisplayName("Deve lançar EntityExistsException se o novo e-mail já estiver em uso")
        void shouldThrowExceptionWhenNewEmailExists() {
            AdminUpdateRequest request = new AdminUpdateRequest(null, "existente@test.com", null, null);

            when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));
            when(adminRepository.findByEmail("existente@test.com")).thenReturn(Optional.of(new Admin()));

            assertThrows(EntityExistsException.class, () -> adminUserService.update(adminId, request));
            verify(adminRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("toggleStatus")
    class ToggleStatus {

        @Test
        @DisplayName("Deve alterar status de ACTIVE para DISABLED")
        void shouldToggleActiveToDisabled() {
            admin.setStatus(UserStatus.ACTIVE);
            when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

            adminUserService.toggleStatus(adminId);

            assertEquals(UserStatus.DISABLED, admin.getStatus());
            verify(adminRepository).save(admin);
        }

        @Test
        @DisplayName("Deve alterar status de DISABLED para ACTIVE")
        void shouldToggleDisabledToActive() {
            admin.setStatus(UserStatus.DISABLED);
            when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

            adminUserService.toggleStatus(adminId);

            assertEquals(UserStatus.ACTIVE, admin.getStatus());
            verify(adminRepository).save(admin);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("Deve deletar admin por id")
        void shouldDeleteAdmin() {
            adminUserService.delete(adminId);

            verify(adminRepository).deleteById(adminId);
        }
    }
}
