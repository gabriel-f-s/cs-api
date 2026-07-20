package com.dev.cs_api.identity.auth.services;

import com.dev.cs_api.core.security.SecurityUtils;
import com.dev.cs_api.identity.auth.dtos.MfaSetupResponse;
import com.dev.cs_api.identity.auth.exceptions.InvalidTokenException;
import com.dev.cs_api.identity.user.models.User;
import com.dev.cs_api.identity.user.repositories.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class MfaService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    private final UserRepository userRepository;

    public MfaService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, UserRepository userRepository) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
    }

    @Transactional
    public MfaSetupResponse generateSetup() {
        User user = findUser();

        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MFA já está ativado para este usuário.");
        }

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();
        user.setMfaSecret(secret);
        userRepository.save(user);
        String otpAuthUri = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL("Controle Smart", user.getEmail(), key);

        return new MfaSetupResponse(secret, otpAuthUri);
    }

    @Transactional
    public void confirmSetup(String code) {
        User user = findUser();
        if (user.getMfaSecret() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Processo de setup do MFA não foi iniciado.");

        boolean isCodeValid = verifyMfaCode(user.getMfaSecret(), code);

        if (!isCodeValid)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código incorreto. Tente novamente.");

        user.setMfaEnabled(true);
        userRepository.save(user);
    }

    public String generateMfaToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cs-api-auth")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .subject(user.getId().toString())
                .claim("purpose", "MFA_VERIFICATION")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean verifyMfaCode(String secret, String code) {
        GoogleAuthenticator authenticator = new GoogleAuthenticator();
        return authenticator.authorize(secret, Integer.parseInt(code));
    }

    public UUID extractUserIdFromMfaToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            if (!"MFA_VERIFICATION".equals(jwt.getClaims().get("purpose"))) {
                throw new InvalidTokenException("Token inválido para esta operação");
            }
            return UUID.fromString(jwt.getSubject());
        } catch (Exception e) {
            throw new InvalidTokenException("Token MFA inválido ou expirado");
        }
    }

    public void disableMfa(String code) {
        User user = findUser();

        boolean isCodeValid = verifyMfaCode(user.getMfaSecret(), code);

        if (!isCodeValid)
            throw new InvalidTokenException( "Código MFA inválido");

        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        userRepository.save(user);
    }

    private User findUser() {
        return userRepository.findById(SecurityUtils.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
