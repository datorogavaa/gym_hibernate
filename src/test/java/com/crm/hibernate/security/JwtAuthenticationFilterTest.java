package com.crm.hibernate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtService jwtService;
    @Mock ObjectProvider<UserDetailsService> provider;
    @Mock UserDetailsService userDetailsService;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenSetsAuthentication() throws ServletException, IOException {
        UserDetails user = User.withUsername("bob.taylor").password("secret").roles("TRAINEE").build();
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(provider.getObject()).thenReturn(userDetailsService);
        doReturn(user).when(userDetailsService).loadUserByUsername("bob.taylor");
        when(jwtService.extractUsername("token")).thenReturn("bob.taylor");
        when(jwtService.isValid(eq("token"), eq(user))).thenReturn(true);

        new JwtAuthenticationFilter(jwtService, provider)
                .doFilterInternal(request, response, filterChain);

        assertEquals("bob.taylor", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidBearerTokenLeavesContextEmpty() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");
        when(jwtService.extractUsername("invalid")).thenThrow(new IllegalArgumentException("invalid token"));

        new JwtAuthenticationFilter(jwtService, provider)
                .doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
