package com.crm.hibernate.exception;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private static final String TEST_TX_ID = "test-tx-12345";

    @BeforeEach
    void setUp() {
        MDC.put("transactionId", TEST_TX_ID);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("SecurityException handling returns 401 Unauthorized JSON")
    void handleSecurityException_ReturnsUnauthorized() {
        SecurityException ex = new SecurityException("Unauthorized access");

        ResponseEntity<String> response = exceptionHandler.handleSecurityException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":401"));
        assertTrue(response.getBody().contains("\"error\":\"Unauthorized access\""));
        assertTrue(response.getBody().contains("\"transactionId\":\"" + TEST_TX_ID + "\""));
    }

    @Test
    @DisplayName("IllegalArgumentException handling returns 400 Bad Request JSON")
    void handleBadRequest_ReturnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("Specialization ID is required");

        ResponseEntity<String> response = exceptionHandler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":400"));
        assertTrue(response.getBody().contains("\"error\":\"Specialization ID is required\""));
        assertTrue(response.getBody().contains("\"transactionId\":\"" + TEST_TX_ID + "\""));
    }

    @Test
    @DisplayName("NoSuchElementException handling returns 404 Not Found JSON")
    void handleNotFound_ReturnsNotFound() {
        NoSuchElementException ex = new NoSuchElementException("Trainer not found");

        ResponseEntity<String> response = exceptionHandler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":404"));
        assertTrue(response.getBody().contains("\"error\":\"Trainer not found\""));
        assertTrue(response.getBody().contains("\"transactionId\":\"" + TEST_TX_ID + "\""));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException handling formatting field errors returns 400 JSON")
    void handleValidation_ReturnsFormattedValidationErrors() throws NoSuchMethodException {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "trainerDto");
        bindingResult.addError(new FieldError("trainerDto", "firstName", "must not be blank"));
        bindingResult.addError(new FieldError("trainerDto", "specialization", "id is required"));

        Method method = this.getClass().getDeclaredMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<String> response = exceptionHandler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":400"));
        assertTrue(response.getBody().contains("firstName: must not be blank; specialization: id is required"));
    }

    @Test
    @DisplayName("ServletException wrapping IllegalArgumentException unwraps root cause and returns 400 Bad Request")
    void handleServletException_WrappedIllegalArgumentException_ReturnsBadRequest() {
        IllegalArgumentException rootCause = new IllegalArgumentException("Specialization ID is required and must be greater than 0");
        ServletException servletException = new ServletException("Request processing failed", rootCause);

        ResponseEntity<String> response = exceptionHandler.handleServletException(servletException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":400"));
        assertTrue(response.getBody().contains("\"error\":\"Specialization ID is required and must be greater than 0\""));
    }

    @Test
    @DisplayName("ServletException wrapping NoSuchElementException unwraps root cause and returns 404 Not Found")
    void handleServletException_WrappedNoSuchElementException_ReturnsNotFound() {
        NoSuchElementException rootCause = new NoSuchElementException("Entity not found");
        ServletException servletException = new ServletException("Nested error", rootCause);

        ResponseEntity<String> response = exceptionHandler.handleServletException(servletException);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":404"));
        assertTrue(response.getBody().contains("\"error\":\"Entity not found\""));
    }

    @Test
    @DisplayName("ServletException wrapping SecurityException unwraps root cause and returns 401 Unauthorized")
    void handleServletException_WrappedSecurityException_ReturnsUnauthorized() {
        SecurityException rootCause = new SecurityException("Access denied");
        ServletException servletException = new ServletException("Filter failed", rootCause);

        ResponseEntity<String> response = exceptionHandler.handleServletException(servletException);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":401"));
        assertTrue(response.getBody().contains("\"error\":\"Access denied\""));
    }

    @Test
    @DisplayName("ServletException with generic cause returns 500 Internal Server Error")
    void handleServletException_GenericCause_ReturnsInternalServerError() {
        RuntimeException genericCause = new RuntimeException("Database stream error");
        ServletException servletException = new ServletException("Servlet processing failed", genericCause);

        ResponseEntity<String> response = exceptionHandler.handleServletException(servletException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":500"));
        assertTrue(response.getBody().contains("\"error\":\"Unexpected internal server error\""));
    }

    @Test
    @DisplayName("General Exception handling returns 500 Internal Server Error JSON")
    void handleGeneralException_ReturnsInternalServerError() {
        Exception ex = new RuntimeException("Unexpected NPE or system error");

        ResponseEntity<String> response = exceptionHandler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"status\":500"));
        assertTrue(response.getBody().contains("\"error\":\"Unexpected internal server error\""));
    }

    @Test
    @DisplayName("Exception handling escapes special characters and quotes in error messages correctly")
    void error_EscapesSpecialJsonCharacters() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input: \"value\"\nwith new line and \\slash");

        ResponseEntity<String> response = exceptionHandler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Invalid input: \\\"value\\\"\\nwith new line and \\\\slash"));
    }

    @Test
    @DisplayName("Exception handling works correctly when MDC transactionId is null")
    void error_HandlesNullMdcTransactionId() {
        MDC.clear();

        IllegalArgumentException ex = new IllegalArgumentException("Test error");

        ResponseEntity<String> response = exceptionHandler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("\"transactionId\":\"\""));
    }

    private void dummyMethod(String param) {
        // Helper method for MethodParameter in validation test
    }
}