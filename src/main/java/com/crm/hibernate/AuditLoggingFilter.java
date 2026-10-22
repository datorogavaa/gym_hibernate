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
import java.util.regex.Pattern;

public class AuditLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingFilter.class);
    private static final String TRANSACTION_ID = "transactionId";
    private static final String TRANSACTION_HEADER = "X-Transaction-Id";

    private static final Pattern SENSITIVE_JSON_PATTERN = Pattern.compile(
            "\"(?i)(?:[^\"]*password[^\"]*|token|secret|accessToken|refreshToken|authorization)\"\\s*:\\s*(?:\"[^\"]*\"|[^,\\}\\]\\s]+)",
            Pattern.CASE_INSENSITIVE
    );

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

            String requestBody = sanitize(getStringValue(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding()));
            String responseBody = sanitize(getStringValue(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding()));

            log.info("Completed Request: Status=[{}] Time=[{}ms] RequestBody=[{}] ResponseBody=[{}]",
                    response.getStatus(), timeTaken, requestBody, responseBody);

            // Crucial: copy cached response payload back to the original response stream
            responseWrapper.copyBodyToResponse();

            MDC.remove(TRANSACTION_ID);
        }
    }

    private String sanitize(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        // Replaces matched sensitive key-value pairs with "key": "*****"
        return SENSITIVE_JSON_PATTERN.matcher(json).replaceAll(match -> {
            String matchStr = match.group();
            int colonIndex = matchStr.indexOf(':');
            String key = matchStr.substring(0, colonIndex + 1);
            return key + " \"*****\"";
        });
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