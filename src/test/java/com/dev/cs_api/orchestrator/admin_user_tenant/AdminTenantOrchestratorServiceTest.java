package com.dev.cs_api.orchestrator.admin_user_tenant;

import com.dev.cs_api.identity.api.AdminTenantUserApi;
import com.dev.cs_api.identity.api.UserDetailResponse;
import com.dev.cs_api.identity.api.UserSummaryResponse;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.tenancy.api.TenantCompanyResponse;
import com.dev.cs_api.tenancy.api.TenantUserCompanyApi;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminTenantOrchestratorServiceTest {

    @Mock
    private AdminTenantUserApi identityApi;

    @Mock
    private TenantUserCompanyApi tenancyApi;

    @InjectMocks
    private AdminTenantOrchestratorService orchestratorService;

    private UUID userId;
    private UUID tenantId;
    private UserDetailResponse userDetailResponse;
    private UserSummaryResponse userSummaryResponse;
    private TenantCompanyResponse tenantCompanyResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();

        userDetailResponse = new UserDetailResponse(
                userId,
                "John Doe",
                "john@example.com",
                "11999999999",
                "TENANT_ADMIN",
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now(),
                tenantId
        );

        userSummaryResponse = new UserSummaryResponse(
                userId,
                "John Doe",
                "john@example.com",
                "TENANT_ADMIN",
                "ACTIVE",
                LocalDateTime.now(),
                false,
                tenantId
        );

        tenantCompanyResponse = new TenantCompanyResponse(
                tenantId,
                "Trade Name Corp",
                "Corporate Name SA",
                "12345678000199"
        );
    }

    @Nested
    @DisplayName("findOne")
    class FindOne {

        @Test
        @DisplayName("Deve buscar detalhes do usuário e do tenant e combinar o resultado")
        void shouldCombineUserAndTenantDetails() {
            when(identityApi.findOne(userId)).thenReturn(userDetailResponse);
            when(tenancyApi.getTenantDetailsById(tenantId)).thenReturn(tenantCompanyResponse);

            UserWithTenantDetailsResponse result = orchestratorService.findOne(userId);

            assertNotNull(result);
            assertEquals(userId, result.id());
            assertEquals("John Doe", result.name());
            assertEquals(tenantId, result.tenantId());
            assertEquals("Trade Name Corp", result.tenantTradeName());
            assertEquals("12345678000199", result.tenantDocument());

            verify(identityApi).findOne(userId);
            verify(tenancyApi).getTenantDetailsById(tenantId);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve buscar todos os usuários sem filtro por tenantId")
        void shouldFindAllWithoutTenantIdFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserSummaryResponse> usersPage = new PageImpl<>(List.of(userSummaryResponse));

            when(identityApi.findAll(pageable)).thenReturn(usersPage);
            when(tenancyApi.getTenantDetails(List.of(tenantId))).thenReturn(Map.of(tenantId, tenantCompanyResponse));

            Page<UserWithTenantSummaryResponse> result = orchestratorService.findAll(null, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            UserWithTenantSummaryResponse item = result.getContent().getFirst();
            assertEquals("John Doe", item.name());
            assertEquals("Trade Name Corp", item.tenantTradeName());

            verify(identityApi).findAll(pageable);
            verify(tenancyApi).getTenantDetails(List.of(tenantId));
        }

        @Test
        @DisplayName("Deve buscar usuários filtrando por tenantId")
        void shouldFindAllWithTenantIdFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserSummaryResponse> usersPage = new PageImpl<>(List.of(userSummaryResponse));

            when(identityApi.findAllByTenantId(tenantId, pageable)).thenReturn(usersPage);
            when(tenancyApi.getTenantDetails(List.of(tenantId))).thenReturn(Map.of(tenantId, tenantCompanyResponse));

            Page<UserWithTenantSummaryResponse> result = orchestratorService.findAll(tenantId, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(identityApi).findAllByTenantId(tenantId, pageable);
            verify(tenancyApi).getTenantDetails(List.of(tenantId));
        }
    }

    @Nested
    @DisplayName("toggleStatus")
    class ToggleStatus {

        @Test
        @DisplayName("Deve delegar chamada de toggleStatus para a identityApi")
        void shouldDelegateToggleStatus() {
            orchestratorService.toggleStatus(userId);

            verify(identityApi).toggleStatus(userId);
        }
    }
}
