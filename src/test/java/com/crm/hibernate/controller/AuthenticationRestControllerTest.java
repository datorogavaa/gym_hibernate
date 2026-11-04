package com.crm.hibernate.controller;

import com.crm.hibernate.dto.LoginRequestDto;
import com.crm.hibernate.dto.LoginResponseDto;
import com.crm.hibernate.restcontrollers.AuthenticationRestController;
import com.crm.hibernate.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationRestControllerTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @InjectMocks AuthenticationRestController controller;

    @Test
    void loginReturnsBearerToken() {
        UserDetails user = User.withUsername("bob.taylor").password("secret").roles("TRAINEE").build();
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("token");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        var response = controller.login(new LoginRequestDto("bob.taylor", "secret"));

        assertEquals("token", response.getBody().token());
        assertEquals("Bearer", response.getBody().tokenType());
        assertEquals(3600, response.getBody().expiresIn());
    }

    @Test
    void logoutRevokesBearerToken() {
        var response = controller.logout("Bearer token");

        assertEquals(204, response.getStatusCode().value());
        verify(jwtService).revoke("token");
    }
}
