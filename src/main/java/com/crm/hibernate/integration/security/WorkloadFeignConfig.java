package com.crm.hibernate.integration.security;

import com.crm.hibernate.integration.logging.TransactionLoggingFilter;
import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

/** Adds the Bearer token and propagates the transactionId to the downstream service. */
public class WorkloadFeignConfig {

    @Bean
    RequestInterceptor workloadRequestInterceptor(ServiceJwtProvider jwtProvider) {
        return template -> {
            template.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtProvider.generateToken());
            String txId = MDC.get(TransactionLoggingFilter.MDC_KEY);
            if (txId != null) {
                template.header(TransactionLoggingFilter.TRANSACTION_HEADER, txId);
            }
        };
    }
}
