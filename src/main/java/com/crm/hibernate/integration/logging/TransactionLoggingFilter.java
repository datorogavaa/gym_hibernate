package com.crm.hibernate.integration.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Transaction-level logging: generates (or accepts) a transactionId, puts it into MDC so every log line
 * of the request carries it, echoes it in the response header, and logs endpoint, request body,
 * response status and response body. Runs before Spring Security so 401/403 are logged too.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TransactionLoggingFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_HEADER = "X-Transaction-Id";
    public static final String MDC_KEY = "transactionId";
    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String txId = request.getHeader(TRANSACTION_HEADER);
        if (txId == null || txId.isBlank()) {
            txId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_KEY, txId);
        response.setHeader(TRANSACTION_HEADER, txId);

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);
        log.info("[transaction] START {} {}{}", request.getMethod(), request.getRequestURI(),
                request.getQueryString() == null ? "" : "?" + request.getQueryString());
        try {
            chain.doFilter(req, res);
        } finally {
            String reqBody = new String(req.getContentAsByteArray(), StandardCharsets.UTF_8);
            String resBody = new String(res.getContentAsByteArray(), StandardCharsets.UTF_8);
            log.info("[transaction] END {} {} request={} -> status={} response={}",
                    request.getMethod(), request.getRequestURI(), reqBody, res.getStatus(), resBody);
            res.copyBodyToResponse();
            MDC.remove(MDC_KEY);
        }
    }
}

