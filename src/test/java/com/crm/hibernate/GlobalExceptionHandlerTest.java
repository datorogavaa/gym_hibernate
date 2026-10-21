package com.crm.hibernate.exception;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void handleSecurityExceptionWithoutMdcReturnsPlainText() {
        SecurityException ex = new SecurityException("Access Denied");

        ResponseEntity<String> response = exceptionHandler.handleSecurityException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Access Denied", response.getBody());
        assertNull(response.getHeaders().getFirst("Content-Type"));
    }

    @Test
    void handleSecurityExceptionWithMdcReturnsJson() {
        MDC.put("transactionId", "tx-12345");
        SecurityException ex = new SecurityException("Unauthorized access");

        ResponseEntity<String> response = exceptionHandler.handleSecurityException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("application/json", response.getHeaders().getFirst("Content-Type"));
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":401"));
        assertTrue(response.getBody().contains("\"error\":\"Unauthorized access\""));
        assertTrue(response.getBody().contains("\"transactionId\":\"tx-12345\""));
    }

    @Test
    void handleBadRequestReturns400() {
        MDC.put("transactionId", "tx-bad-req");
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");

        ResponseEntity<String> response = exceptionHandler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":400"));
        assertTrue(response.getBody().contains("\"error\":\"Invalid argument provided\""));
    }

    @Test
    void handleNotFoundReturns404() {
        MDC.put("transactionId", "tx-not-found");
        NoSuchElementException ex = new NoSuchElementException("Resource missing");

        ResponseEntity<String> response = exceptionHandler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":404"));
        assertTrue(response.getBody().contains("\"error\":\"Resource missing\""));
    }

    @Test
    void handleValidationFormattedErrors() throws NoSuchMethodException {
        MDC.put("transactionId", "tx-validation");

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "targetObject");
        bindingResult.addError(new FieldError("targetObject", "firstName", "must not be empty"));
        bindingResult.addError(new FieldError("targetObject", "age", "must be greater than 0"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("handleValidationFormattedErrors"), -1
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<String> response = exceptionHandler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("firstName: must not be empty; age: must be greater than 0"));
    }

    @Test
    void handleNullMessageAndEscapesSpecialCharactersInJson() {
        MDC.put("transactionId", "tx-\"quote\"-\\slash");
        IllegalArgumentException ex = new IllegalArgumentException((String) null);

        ResponseEntity<String> response = exceptionHandler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"error\":\"Request failed\""));
        assertTrue(response.getBody().contains("\"transactionId\":\"tx-\\\"quote\\\"-\\\\slash\""));
    }
}