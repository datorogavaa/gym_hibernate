package com.crm.hibernate.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";

    @Test
    void generatedTokenContainsUsernameAndIsValid() {
        JwtService jwtService = new JwtService(SECRET, Duration.ofHours(1));
        UserDetailsFixture user = new UserDetailsFixture();

        String token = jwtService.generateToken(user);

        assertEquals("bob.taylor", jwtService.extractUsername(token));
        assertTrue(jwtService.isValid(token, user));
        assertEquals(3600, jwtService.getExpirationSeconds());
    }

    @Test
    void revokedTokenIsNoLongerValid() {
        JwtService jwtService = new JwtService(SECRET, Duration.ofHours(1));
        UserDetailsFixture user = new UserDetailsFixture();
        String token = jwtService.generateToken(user);

        jwtService.revoke(token);

        assertFalse(jwtService.isValid(token, user));
    }

    private static final class UserDetailsFixture extends User {
        private UserDetailsFixture() {
            super("bob.taylor", "secret", java.util.List.of(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_TRAINEE")));
        }
    }
}
