package com.crm.hibernate;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuditLoggingFilter auditLoggingFilter;

    @Test
    void filterUsesExistingTransactionIdAndClearsMdc() throws ServletException, IOException {
        when(request.getHeader("X-Transaction-Id")).thenReturn("transaction-123");
        when(request.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");

        auditLoggingFilter.doFilterInternal(request, response, filterChain);

        verify(response).addHeader("X-Transaction-Id", "transaction-123");
        verify(filterChain).doFilter(any(), any());
        assertNull(MDC.get("transactionId"));
    }

    @Test
    void filterGeneratesTransactionIdWhenRequestDoesNotProvideOne() throws ServletException, IOException {
        when(request.getHeader("X-Transaction-Id")).thenReturn(" ");
        when(request.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");

        auditLoggingFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<String> transactionId = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(org.mockito.ArgumentMatchers.eq("X-Transaction-Id"), transactionId.capture());
        assertTrue(transactionId.getValue().matches("[0-9a-f-]{36}"));
        assertNull(MDC.get("transactionId"));
    }

    @Test
    void filterClearsMdcWhenChainThrows() throws ServletException, IOException {
        when(request.getHeader("X-Transaction-Id")).thenReturn("transaction-123");
        when(request.getCharacterEncoding()).thenReturn("UTF-8");
        when(response.getCharacterEncoding()).thenReturn("UTF-8");
        doThrow(new ServletException("failure")).when(filterChain).doFilter(any(), any());

        ServletException exception = assertThrows(ServletException.class,
                () -> auditLoggingFilter.doFilterInternal(request, response, filterChain));

        assertEquals("failure", exception.getMessage());
        assertNull(MDC.get("transactionId"));
    }
}
