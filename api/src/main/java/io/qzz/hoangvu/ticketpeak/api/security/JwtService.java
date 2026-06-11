package io.qzz.hoangvu.ticketpeak.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final String ROLE_CLAIM = "role";

    private final SecretKey signingKey;
    private final long accessTokenExpirySeconds;
    private final long refreshTokenExpirySeconds;

    public JwtService(
            @Value("${app.jwt.secret:ticketpeak-development-secret-key-ticketpeak-development-secret-key}") String secret,
            @Value("${app.jwt.access-token-expiry:900}") long accessTokenExpirySeconds,
            @Value("${app.jwt.refresh-token-expiry:604800}") long refreshTokenExpirySeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;
    }

    public JwtTokenPair issueTokenPair(UUID accountId, Set<Role> roles) {
        Instant now = Instant.now();
        String refreshTokenId = UUID.randomUUID().toString();

        String rolesClaim = roles.stream().map(Role::name).collect(Collectors.joining(","));

        String accessToken = Jwts.builder()
                .subject(accountId.toString())
                .claim(ROLE_CLAIM, rolesClaim)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpirySeconds)))
                .signWith(signingKey)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(accountId.toString())
                .id(refreshTokenId)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTokenExpirySeconds)))
                .signWith(signingKey)
                .compact();

        return new JwtTokenPair(accessToken, refreshToken, refreshTokenId);
    }

    public ParsedAccessToken parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        String rolesValue = claims.get(ROLE_CLAIM, String.class);
        if (rolesValue == null || rolesValue.isBlank()) {
            throw new JwtException("Missing role claim");
        }

        Set<Role> roles = Arrays.stream(rolesValue.split(","))
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        return new ParsedAccessToken(
                UUID.fromString(claims.getSubject()),
                roles
        );
    }

    public ParsedRefreshToken parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        String type = claims.get(TOKEN_TYPE_CLAIM, String.class);
        if (!TOKEN_TYPE_REFRESH.equals(type)) {
            throw new JwtException("Invalid token type");
        }

        String tokenId = claims.getId();
        if (tokenId == null || tokenId.isBlank()) {
            throw new JwtException("Missing token id");
        }

        return new ParsedRefreshToken(
                UUID.fromString(claims.getSubject()),
                tokenId
        );
    }

    public long accessTokenExpirySeconds() {
        return accessTokenExpirySeconds;
    }

    public long refreshTokenExpirySeconds() {
        return refreshTokenExpirySeconds;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record ParsedAccessToken(UUID accountId, Set<Role> roles) {
    }

    public record ParsedRefreshToken(UUID accountId, String refreshTokenId) {
    }
}
