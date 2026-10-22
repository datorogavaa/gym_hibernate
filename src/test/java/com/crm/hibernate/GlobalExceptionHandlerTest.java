package com.crm.hibernate;

import com.crm.hibernate.dto.ErrorResponseDto;
import com.crm.hibernate.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleSecurityExceptionReturnsUnauthorized() {
        SecurityException exception = new SecurityException("Unauthorized access");

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handleSecurityException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
        assertEquals("Unauthorized", response.getBody().getError());
        assertEquals("Unauthorized access", response.getBody().getMessage());
    }

    @Test
    void handleBadRequestReturnsBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input data");

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Invalid input data", response.getBody().getMessage());
    }

    @Test
    void handleNotFoundReturnsNotFound() {
        NoSuchElementException exception = new NoSuchElementException("User not found");

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void handleGeneralExceptionReturnsInternalServerError() {
        Exception exception = new RuntimeException("Database connection timeout");

        ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handleGeneralException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An internal server error occurred.", response.getBody().getMessage());
    }

    @Test
    void buildResponseIncludesTransactionIdFromMDC() {
        String mockTransactionId = "tx-12345-abc";

        try (MockedStatic<MDC> mdcMockedStatic = mockStatic(MDC.class)) {
            mdcMockedStatic.when(() -> MDC.get("transactionId")).thenReturn(mockTransactionId);

            SecurityException exception = new SecurityException("Access denied");
            ResponseEntity<ErrorResponseDto> response = globalExceptionHandler.handleSecurityException(exception);

            assertNotNull(response.getBody());
            assertEquals(mockTransactionId, response.getBody().getTransactionId());
        }
    }
}