package com.dev.cs_api.identity.services;

import com.dev.cs_api.identity.exceptions.InvalidTokenException;
import com.dev.cs_api.identity.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.access-token.expiration-minutes:15}")
    private long accessTokenExpiration;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        List<String> authorities = new ArrayList<>();
        user.getRole().getPermissions().forEach(permission -> {
            authorities.add(permission.getName());
        });
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
                .issuer("cs-api-auth")
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenExpiration, ChronoUnit.MINUTES))
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().getName())
                .claim("authorities", authorities);

        if (user.getTenantId() != null) {
            builder.claim("tenantId", user.getTenantId());
        }
        JwtClaimsSet claims = builder.build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateTemporaryToken(User user, String purpose) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cs-api-auth")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                .subject(user.getId().toString())
                .claim("purpose", purpose)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public UUID extractUserIdFromTempToken(String token, String expectedPurpose) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            String tokenPurpose = jwt.getClaimAsString("purpose");
            if (!expectedPurpose.equals(tokenPurpose)) {
                throw new InvalidTokenException("O token fornecido não é válido para esta operação.");
            }

            return UUID.fromString(jwt.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Token temporário inválido ou expirado.");
        }
    }
}

