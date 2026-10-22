package com.crm.hibernate.exception;

import com.crm.hibernate.dto.ErrorResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRANSACTION_ID = "transactionId";

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponseDto> handleSecurityException(SecurityException ex) {
        log.warn("Authentication or authorization failure: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Invalid request payload: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(NoSuchElementException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception ex) {
        log.error("Unhandled internal server exception: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred.");
    }

    private ResponseEntity<ErrorResponseDto> buildResponse(HttpStatus status, String message) {
        String transactionId = MDC.get(TRANSACTION_ID);

        ErrorResponseDto errorResponse = new ErrorResponseDto(
                status.value(),
                status.getReasonPhrase(),
                message,
                transactionId
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}