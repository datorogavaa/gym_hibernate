package com.crm.hibernate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class AuditLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingFilter.class);
    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_HEADER = "X-Transaction-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String transactionId = request.getHeader(TRANSACTION_HEADER);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TRANSACTION_ID, transactionId);
        response.addHeader(TRANSACTION_HEADER, transactionId);

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            log.info("Incoming Request: Method=[{}] URI=[{}]", request.getMethod(), request.getRequestURI());

            filterChain.doFilter(requestWrapper, responseWrapper);

        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;

            // ნაბიჯი 1: ჯერ აუცილებლად უნდა გადაკოპირდეს ბუფერი რეალურ response-ში!
            responseWrapper.copyBodyToResponse();

            // ნაბიჯი 2: წაკითხვა ხდება copyBodyToResponse()-ის შემდეგ
            String requestBody = sanitize(getStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding()));
            String responseBody = sanitize(getStringValue(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding()));

            log.info("Completed Request: Status=[{}] Time=[{}ms] RequestBody=[{}] ResponseBody=[{}]",
                    response.getStatus(), timeTaken, requestBody, responseBody);

            MDC.remove(TRANSACTION_ID);
        }
    }

    private String sanitize(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        return body.replaceAll("(?i)(\"(?:password|oldPassword|newPassword|authPass)\"\\s*:\\s*\")[^\"]*(\")",
                "$1****$2");
    }

    private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            if (contentAsByteArray == null || contentAsByteArray.length == 0) {
                return "";
            }
            return new String(contentAsByteArray, characterEncoding != null ? characterEncoding : "UTF-8")
                    .replaceAll("[\\r\\n\\t]+", " ");
        } catch (UnsupportedEncodingException e) {
            return "[Unsupported Encoding]";
        }
    }
}