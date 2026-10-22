package com.crm.hibernate.controller;

import com.crm.hibernate.restcontrollers.AuthenticationRestController;
import com.crm.hibernate.security.AuthenticationService;
import com.crm.hibernate.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationRestControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationRestController authenticationRestController;

    @Test
    void loginReturnsOkWhenCredentialsAreValid() {
        ResponseEntity<Void> response = authenticationRestController.login("john.doe", "Pass12345!");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authenticationService).authenticate("john.doe", "Pass12345!");
    }

    @Test
    void changePasswordReturnsOkWhenChangeSucceeds() {
        ResponseEntity<Void> response = authenticationRestController.changePassword("john.doe", "Pass12345!", "NewPass123!");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).changePassword("john.doe", "Pass12345!", "john.doe", "NewPass123!");
    }
}
