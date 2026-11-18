package com.crm.hibernate.integration.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/** Issues short-lived HS256 tokens identifying this service to downstream microservices. */
@Component
public class ServiceJwtProvider {

    private final SecretKey key;
    private final long expirationMs;
    private final String serviceName;

    public ServiceJwtProvider(@Value("${security.jwt.secret_integration:Z3ltLWNybS13b3JrbG9hZC1zZWNyZXQta2V5LW1pbi0yNTYtYml0cy1sb25nLSEhIQ==}") String secret,
                              @Value("${security.jwt.expiration-ms_integration:300000}") long expirationMs,
                              @Value("${spring.application.name:hibernate}") String serviceName) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMs = expirationMs;
        this.serviceName = serviceName;
    }

    public String generateToken() {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(serviceName)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(key)
                .compact();
    }
}
