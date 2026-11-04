package com.crm.hibernate.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final Duration expiration;
    private final ConcurrentMap<String, Date> revokedTokens = new ConcurrentHashMap<>();

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration:PT1H}") Duration expiration) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
    }

    public String generateToken(UserDetails userDetails) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expiration.toMillis());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .id(UUID.randomUUID().toString())
                .claim("roles", roles)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token);
        return userDetails.getUsername().equals(claims.getSubject())
                && claims.getExpiration().after(new Date())
                && !isRevoked(claims);
    }

    public void revoke(String token) {
        Claims claims = parseClaims(token);
        String tokenId = claims.getId();
        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException("JWT does not contain a token identifier");
        }
        revokedTokens.put(tokenId, claims.getExpiration());
    }

    private boolean isRevoked(Claims claims) {
        removeExpiredRevocations();
        return claims.getId() != null && revokedTokens.containsKey(claims.getId());
    }

    private void removeExpiredRevocations() {
        Date now = new Date();
        revokedTokens.entrySet().removeIf(entry -> !entry.getValue().after(now));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }
}
