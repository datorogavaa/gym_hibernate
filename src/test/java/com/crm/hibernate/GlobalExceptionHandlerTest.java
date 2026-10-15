package com.crm.hibernate;

import com.crm.hibernate.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleSecurityExceptionReturnsUnauthorized() {
        ResponseEntity<String> response = handler.handleSecurityException(new SecurityException("Bad credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Bad credentials", response.getBody());
    }

    @Test
    void handleBadRequestReturnsBadRequest() {
        ResponseEntity<String> response = handler.handleBadRequest(new IllegalArgumentException("Missing required field"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing required field", response.getBody());
    }

    @Test
    void handleNotFoundReturnsNotFound() {
        ResponseEntity<String> response = handler.handleNotFound(new NoSuchElementException("User not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());
    }
}
