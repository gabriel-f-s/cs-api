package com.dev.cs_api.identity.auth.services;

import com.dev.cs_api.identity.auth.dtos.*;
import com.dev.cs_api.identity.auth.enums.AuthStatus;
import com.dev.cs_api.identity.auth.exceptions.InvalidTokenException;
import com.dev.cs_api.identity.auth.models.RefreshToken;
import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.enums.UserStatus;
import com.dev.cs_api.identity.user.models.Admin;
import com.dev.cs_api.identity.user.models.Role;
import com.dev.cs_api.identity.user.models.User;
import com.dev.cs_api.identity.user.repositories.AdminRepository;
import com.dev.cs_api.identity.user.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private MfaService mfaService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Admin admin;
    private Role userRole;
    private Role systemAdminRole;
    private UUID userId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName(RoleName.OPERATOR);

        systemAdminRole = new Role();
        systemAdminRole.setId(2L);
        systemAdminRole.setName(RoleName.SYSTEM_ADMIN);

        user = new User();
        user.setId(userId);
        user.setName("Normal User");
        user.setEmail("user@test.com");
        user.setPassword("encodedPassword");
        user.setRole(userRole);
        user.setStatus(UserStatus.ACTIVE);
        user.setForcePasswordChange(false);
        user.setMfaEnabled(false);

        admin = new Admin();
        admin.setId(adminId);
        admin.setName("Admin User");
        admin.setEmail("admin@test.com");
        admin.setPassword("encodedPassword");
        admin.setRole(systemAdminRole);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setFailedLoginAttempts(0);
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("Deve realizar login com sucesso para usuário normal")
        void shouldLoginSuccessfullyForUser() {
            LoginRequest request = new LoginRequest("user@test.com", "pass123");
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken("refresh-token-123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(jwtService.generateAccessToken(user)).thenReturn("access-token-123");
            when(refreshTokenService.create(user)).thenReturn(refreshToken);

            AuthResponse response = authService.login(request, "Mozilla/5.0", "127.0.0.1");

            assertNotNull(response);
            assertEquals("access-token-123", response.accessToken());
            assertEquals("refresh-token-123", response.refreshToken());
            assertEquals(AuthStatus.SUCCESS, response.status());
            assertFalse(response.mfaRequired());
        }

        @Test
        @DisplayName("Deve solicitar troca de senha quando forcePasswordChange for verdadeiro")
        void shouldRequirePasswordChangeWhenFlagIsTrue() {
            user.setForcePasswordChange(true);
            LoginRequest request = new LoginRequest("user@test.com", "pass123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(jwtService.generateTemporaryToken(user, AuthStatus.REQUIRE_PASSWORD_CHANGE.getStatus())).thenReturn("temp-token-123");

            AuthResponse response = authService.login(request, "Mozilla/5.0", "127.0.0.1");

            assertNotNull(response);
            assertEquals("temp-token-123", response.accessToken());
            assertEquals(AuthStatus.REQUIRE_PASSWORD_CHANGE, response.status());
            assertNull(response.refreshToken());
        }

        @Test
        @DisplayName("Deve solicitar MFA quando mfaEnabled for verdadeiro")
        void shouldRequireMfaWhenFlagIsTrue() {
            user.setMfaEnabled(true);
            LoginRequest request = new LoginRequest("user@test.com", "pass123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(mfaService.generateMfaToken(user)).thenReturn("mfa-token-123");

            AuthResponse response = authService.login(request, "Mozilla/5.0", "127.0.0.1");

            assertNotNull(response);
            assertTrue(response.mfaRequired());
            assertEquals("mfa-token-123", response.mfaToken());
            assertEquals(AuthStatus.MFA_REQUIRED, response.status());
        }

        @Test
        @DisplayName("Deve lançar BadCredentialsException e incrementar falhas para Admin no login malsucedido")
        void shouldProcessFailedLoginForAdmin() {
            LoginRequest request = new LoginRequest("admin@test.com", "wrongpass");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Senha incorreta"));
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

            assertThrows(BadCredentialsException.class, () -> authService.login(request, "Mozilla/5.0", "127.0.0.1"));

            assertEquals(1, admin.getFailedLoginAttempts());
            verify(userRepository).save(admin);
        }

        @Test
        @DisplayName("Deve bloquear conta do Admin após 3 tentativas malsucedidas")
        void shouldLockAdminAccountAfterThreeFailures() {
            admin.setFailedLoginAttempts(2);
            LoginRequest request = new LoginRequest("admin@test.com", "wrongpass");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Senha incorreta"));
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

            assertThrows(BadCredentialsException.class, () -> authService.login(request, "Mozilla/5.0", "127.0.0.1"));

            assertEquals(3, admin.getFailedLoginAttempts());
            assertNotNull(admin.getLockedUntil());
            assertTrue(admin.getLockedUntil().isAfter(LocalDateTime.now()));
            verify(userRepository).save(admin);
        }
    }

    @Nested
    @DisplayName("refresh")
    class Refresh {

        @Test
        @DisplayName("Deve renovar tokens com sucesso se refresh token for válido")
        void shouldRefreshTokensSuccessfully() {
            RefreshRequest request = new RefreshRequest("valid-refresh-token");
            RefreshToken existingToken = new RefreshToken();
            existingToken.setToken("valid-refresh-token");
            existingToken.setExpiresAt(Instant.now().plusSeconds(3600));
            existingToken.setUser(user);

            RefreshToken newToken = new RefreshToken();
            newToken.setToken("new-refresh-token");

            when(refreshTokenService.findByToken("valid-refresh-token")).thenReturn(existingToken);
            when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
            when(refreshTokenService.create(user)).thenReturn(newToken);

            AuthResponse response = authService.refresh(request);

            assertNotNull(response);
            assertEquals("new-access-token", response.accessToken());
            assertEquals("new-refresh-token", response.refreshToken());
            assertEquals(AuthStatus.REFRESH_TOKEN, response.status());
        }

        @Test
        @DisplayName("Deve lançar InvalidTokenException se o refresh token estiver expirado")
        void shouldThrowExceptionWhenRefreshTokenExpired() {
            RefreshRequest request = new RefreshRequest("expired-token");
            RefreshToken expiredToken = new RefreshToken();
            expiredToken.setToken("expired-token");
            expiredToken.setExpiresAt(Instant.now().minusSeconds(10));

            when(refreshTokenService.findByToken("expired-token")).thenReturn(expiredToken);

            assertThrows(InvalidTokenException.class, () -> authService.refresh(request));
            verify(refreshTokenService).delete(expiredToken);
        }
    }

    @Nested
    @DisplayName("verifyMfaAndLogin")
    class VerifyMfaAndLogin {

        @Test
        @DisplayName("Deve autenticar com sucesso quando o código MFA estiver correto")
        void shouldLoginWhenMfaCodeIsValid() {
            user.setMfaSecret("SECRET");
            MfaVerifyRequest request = new MfaVerifyRequest("mfa-token", "123456");
            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken("new-refresh");

            when(mfaService.extractUserIdFromMfaToken("mfa-token")).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(mfaService.verifyMfaCode("SECRET", "123456")).thenReturn(true);
            when(jwtService.generateAccessToken(user)).thenReturn("access-token");
            when(refreshTokenService.create(user)).thenReturn(refreshToken);

            AuthResponse response = authService.verifyMfaAndLogin(request, "Agent", "127.0.0.1");

            assertNotNull(response);
            assertEquals("access-token", response.accessToken());
            assertEquals(AuthStatus.SUCCESS, response.status());
        }

        @Test
        @DisplayName("Deve lançar InvalidTokenException se o código MFA for inválido")
        void shouldThrowExceptionWhenMfaCodeIsInvalid() {
            user.setMfaSecret("SECRET");
            MfaVerifyRequest request = new MfaVerifyRequest("mfa-token", "999999");

            when(mfaService.extractUserIdFromMfaToken("mfa-token")).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(mfaService.verifyMfaCode("SECRET", "999999")).thenReturn(false);

            assertThrows(InvalidTokenException.class, () -> authService.verifyMfaAndLogin(request, "Agent", "127.0.0.1"));
        }
    }

    @Nested
    @DisplayName("changeFirstPassword")
    class ChangeFirstPassword {

        @Test
        @DisplayName("Deve lançar BadCredentialsException se as senhas não conferem")
        void shouldThrowExceptionWhenPasswordsDoNotMatch() {
            FirstPasswordChangeRequest request = new FirstPasswordChangeRequest("temp-token", "pass1", "pass2");

            lenient().when(jwtService.extractUserIdFromTempToken(any(), any())).thenReturn(userId);
            lenient().when(userRepository.findById(any())).thenReturn(Optional.of(user));

            assertThrows(BadCredentialsException.class, () -> authService.changeFirstPassword(request, "Agent", "127.0.0.1"));
        }
    }
}
