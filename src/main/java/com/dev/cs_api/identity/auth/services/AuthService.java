package com.dev.cs_api.identity.auth.services;

import com.dev.cs_api.identity.auth.dtos.*;
import com.dev.cs_api.identity.auth.enums.AuthStatus;
import com.dev.cs_api.identity.auth.exceptions.InvalidTokenException;
import com.dev.cs_api.identity.user.models.Admin;
import com.dev.cs_api.identity.auth.models.RefreshToken;
import com.dev.cs_api.identity.user.models.User;
import com.dev.cs_api.identity.user.enums.RoleName;
import com.dev.cs_api.identity.user.repositories.AdminRepository;
import com.dev.cs_api.identity.user.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final MfaService mfaService;

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService, JwtService jwtService, MfaService mfaService, UserRepository userRepository, AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.mfaService = mfaService;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = BadCredentialsException.class)
    public AuthResponse login(LoginRequest request, String userAgent, String ipAddress) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            User user = userRepository.findByEmail(request.email()).orElseThrow();

            if (user.getForcePasswordChange() != null && user.getForcePasswordChange()) {
                String tempToken = jwtService.generateTemporaryToken(user, AuthStatus.REQUIRE_PASSWORD_CHANGE.getStatus());
                return new AuthResponse(
                        tempToken,
                        null,
                        false,
                        null,
                        AuthStatus.REQUIRE_PASSWORD_CHANGE
                );
            }

            if (user.getMfaEnabled() != null && user.getMfaEnabled()) {
                String mfaToken = mfaService.generateMfaToken(user);
                return new AuthResponse(
                        null,
                        null,
                        true,
                        mfaToken,
                        AuthStatus.MFA_REQUIRED
                );
            }

            return processSuccessLogin(user, userAgent, ipAddress);
        } catch (BadCredentialsException e) {
            processFailedLogin(request.email());
            throw new BadCredentialsException(e.getMessage());
        } catch (LockedException e) {
            throw new LockedException(e.getMessage());
        }
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.refreshToken());
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenService.delete(refreshToken);
            throw new InvalidTokenException("Refresh token expirado, por favor, faça login novamente");
        }
        User user = refreshToken.getUser();
        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken newRefreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, newRefreshToken.getToken(), false, null,AuthStatus.REFRESH_TOKEN);
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenService.deleteByToken(request.refreshToken());
    }

    @Transactional
    public AuthResponse verifyMfaAndLogin(MfaVerifyRequest request, String userAgent, String ipAddress) {
        UUID userId = mfaService.extractUserIdFromMfaToken(request.token());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        boolean isCodeValid = mfaService.verifyMfaCode(user.getMfaSecret(), request.code());

        if (!isCodeValid) {
            throw new InvalidTokenException( "Código MFA inválido");
        }
        return  processSuccessLogin(user, userAgent, ipAddress);
    }

    @Transactional
    public AuthResponse changeFirstPassword(FirstPasswordChangeRequest request, String userAgent, String ipAddress) {
        UUID userId = jwtService.extractUserIdFromTempToken(request.tempToken(), AuthStatus.REQUIRE_PASSWORD_CHANGE.getStatus());
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Usuário não encontrado")
        );

        if (!Objects.equals(request.password(), request.confirmPassword()))
            throw new BadCredentialsException("As senhas não conferem");

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setForcePasswordChange(false);
        userRepository.save(user);

        return processSuccessLogin(user, userAgent, ipAddress);
    }

    private AuthResponse processSuccessLogin(User user, String userAgent, String ipAddress) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);
        if (user.getRole().getName() == RoleName.SYSTEM_ADMIN) {
            adminRepository.findById(user.getId()).ifPresent(admin -> {
                admin.setLastLoginAt(LocalDateTime.now());
                admin.setLastLoginUserAgent(userAgent);
                admin.setLastLoginIP(ipAddress);
                admin.setFailedLoginAttempts(0);
                admin.setLockedUntil(null);
                adminRepository.save(admin);
            });
        }
        return new AuthResponse(accessToken, refreshToken.getToken(), false, null, AuthStatus.SUCCESS);
    }

    private void processFailedLogin(String email) {
        userRepository.findByEmail(email)
                .filter(Admin.class::isInstance)
                .map(Admin.class::cast)
                .ifPresent(admin -> {
                    if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
                        return;
                    }
                    int attempts = admin.getFailedLoginAttempts() == null ? 0 : admin.getFailedLoginAttempts();
                    attempts++;
                    admin.setFailedLoginAttempts(attempts);

                    if (attempts >= 3) {
                        admin.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                    }
                    userRepository.save(admin);
                });
    }
}
