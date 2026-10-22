package com.crm.hibernate.exception;

import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(
            SecurityException ex) {

        log.warn("Authentication failure: {}", ex.getMessage());

        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> handleNoHandlerFound(
            NoHandlerFoundException ex) {

        log.warn("No handler found for {} {}", ex.getRequestURL(), ex.getRequestURL());

        return error(
                HttpStatus.NOT_FOUND,
                "Resource not found: " + ex.getRequestURL()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(
            IllegalArgumentException ex) {

        log.warn("Invalid input: {}", ex.getMessage());

        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(
            NoSuchElementException ex) {

        log.warn("Resource not found: {}", ex.getMessage());

        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField()
                        + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<String> handleServletException(
            ServletException ex) {

        Throwable cause = ex;

        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }

        if (cause instanceof IllegalArgumentException) {
            return handleBadRequest((IllegalArgumentException) cause);
        }

        if (cause instanceof NoSuchElementException) {
            return handleNotFound((NoSuchElementException) cause);
        }

        if (cause instanceof SecurityException) {
            return handleSecurityException((SecurityException) cause);
        }

        log.error("Unhandled servlet exception", ex);

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected internal server error"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(
            Exception ex) {

        log.error("Unhandled exception", ex);

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected internal server error"
        );
    }

    private ResponseEntity<String> error(
            HttpStatus status,
            String message) {

        String safeMessage = escapeJson(
                message == null ? "Request failed" : message
        );

        String transactionId = MDC.get("transactionId");

        String safeTransactionId = escapeJson(
                transactionId == null ? "" : transactionId
        );

        String body = "{"
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":" + status.value() + ","
                + "\"error\":\"" + safeMessage + "\","
                + "\"transactionId\":\"" + safeTransactionId + "\""
                + "}";

        return ResponseEntity.status(status)
                .header("Content-Type", "application/json")
                .body(body);
    }


    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}