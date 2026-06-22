package com.dev.cs_api.identity.services;

import com.dev.cs_api.identity.exceptions.InvalidTokenException;
import com.dev.cs_api.identity.models.User;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class MfaService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public MfaService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
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

    public boolean verifyMfaCode(String secret, int code) {
        GoogleAuthenticator authenticator = new GoogleAuthenticator();
        return authenticator.authorize(secret, code);
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
}
