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
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.core.MethodParameter;

import java.util.NoSuchElementException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void handleAccessDeniedReturnsForbidden() {
        ResponseEntity<ErrorResponseDto> response =
                globalExceptionHandler.handleAccessDenied(new AccessDeniedException("denied"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You are not authorized to access this resource.",
                response.getBody().getMessage());
    }

    @Test
    void handleAuthenticationExceptionReturnsInvalidCredentialsMessage() {
        ResponseEntity<ErrorResponseDto> response =
                globalExceptionHandler.handleAuthenticationException(
                        new BadCredentialsException("Bad credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password.", response.getBody().getMessage());
    }

    @Test
    void handleNoResourceFoundIncludesRequestedPath() {
        NoResourceFoundException exception = mock(NoResourceFoundException.class);
        when(exception.getResourcePath()).thenReturn("/api/missing");

        ResponseEntity<ErrorResponseDto> response =
                globalExceptionHandler.handleNoResourceFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No endpoint found for '/api/missing'", response.getBody().getMessage());
    }

    @Test
    void handleMissingRequestParamNamesParameter() {
        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException("year", "int");

        ResponseEntity<ErrorResponseDto> response =
                globalExceptionHandler.handleMissingRequestParam(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Required query parameter 'year' is missing",
                response.getBody().getMessage());
    }

    @Test
    void handleMissingPathVariableNamesVariable() {
        MissingPathVariableException exception = mock(MissingPathVariableException.class);
        when(exception.getVariableName()).thenReturn("username");

        ResponseEntity<ErrorResponseDto> response =
                globalExceptionHandler.handleMissingPathVariable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Required path variable 'username' is missing",
                response.getBody().getMessage());
    }

    @Test
    void handleMethodNotSupportedReturns405() {
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("GET");

        ResponseEntity<ErrorResponseDto> response =
                globalExceptionHandler.handleMethodNotSupported(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("This endpoint only supports POST.", response.getBody().getMessage());
    }

    @Test
    void handleMissingRequestBodyReturnsBadRequest() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("Malformed JSON");

        ResponseEntity<ErrorResponseDto> response =
                globalExceptionHandler.handleMissingRequestBody(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Required request body is missing or malformed",
                response.getBody().getMessage());
    }

    @Test
    void handleValidationExceptionsCollectsFieldErrors() throws Exception {
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyValidationMethod",
                String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult("request", "request");
        bindingResult.addError(new FieldError("request", "username",
                "must not be blank"));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponseDto> response =
                globalExceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed: username: must not be blank",
                response.getBody().getMessage());
    }

    private void dummyValidationMethod(String username) {
    }
}