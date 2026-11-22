package com.crm.hibernate;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLoggingFilterTest {

    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_HEADER = "X-Transaction-Id";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuditLoggingFilter auditLoggingFilter;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void filterPreservesExistingMdcTransactionIdAndDoesNotClearIt() throws ServletException, IOException {
        MDC.put(TRANSACTION_ID, "filter-tx-999");
        when(request.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getHeader(TRANSACTION_HEADER)).thenReturn(null);

        AtomicReference<String> mdcInsideChain = new AtomicReference<>();
        doAnswer(invocation -> {
            mdcInsideChain.set(MDC.get(TRANSACTION_ID));
            return null;
        }).when(filterChain).doFilter(any(), any());

        auditLoggingFilter.doFilterInternal(request, response, filterChain);

        assertEquals("filter-tx-999", mdcInsideChain.get());
        assertEquals("filter-tx-999", MDC.get(TRANSACTION_ID));
        verify(response).setHeader(TRANSACTION_HEADER, "filter-tx-999");
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void filterUsesHeaderTransactionIdWhenMdcIsEmptyAndClearsItAfterwards() throws ServletException, IOException {
        when(request.getHeader(TRANSACTION_HEADER)).thenReturn("transaction-123");
        when(request.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getHeader(TRANSACTION_HEADER)).thenReturn(null);

        AtomicReference<String> mdcInsideChain = new AtomicReference<>();
        doAnswer(invocation -> {
            mdcInsideChain.set(MDC.get(TRANSACTION_ID));
            return null;
        }).when(filterChain).doFilter(any(), any());

        auditLoggingFilter.doFilterInternal(request, response, filterChain);

        assertEquals("transaction-123", mdcInsideChain.get());
        verify(response).setHeader(TRANSACTION_HEADER, "transaction-123");
        verify(filterChain).doFilter(any(), any());
        assertNull(MDC.get(TRANSACTION_ID));
    }

    @Test
    void filterGeneratesTransactionIdWhenNeitherMdcNorHeaderProvidesOne() throws ServletException, IOException {
        when(request.getHeader(TRANSACTION_HEADER)).thenReturn(" ");
        when(request.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getHeader(TRANSACTION_HEADER)).thenReturn(null);

        auditLoggingFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<String> transactionId = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq(TRANSACTION_HEADER), transactionId.capture());
        assertTrue(transactionId.getValue().matches("[0-9a-f-]{36}"));
        assertNull(MDC.get(TRANSACTION_ID));
    }

    @Test
    void filterClearsMdcWhenChainThrowsAndFilterOwnedMdc() throws ServletException, IOException {
        when(request.getHeader(TRANSACTION_HEADER)).thenReturn("transaction-123");
        when(request.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getHeader(TRANSACTION_HEADER)).thenReturn(null);
        doThrow(new ServletException("failure")).when(filterChain).doFilter(any(), any());

        ServletException exception = assertThrows(ServletException.class,
                () -> auditLoggingFilter.doFilterInternal(request, response, filterChain));

        assertEquals("failure", exception.getMessage());
        assertNull(MDC.get(TRANSACTION_ID));
    }

    @Test
    void shouldNotFilterActuatorHealthEndpoint() {
        when(request.getRequestURI()).thenReturn("/actuator/health");

        assertTrue(auditLoggingFilter.shouldNotFilter(request));
    }

    @Test
    void shouldFilterRegularApiEndpoint() {
        when(request.getRequestURI()).thenReturn("/api/trainings");

        assertFalse(auditLoggingFilter.shouldNotFilter(request));
    }
}