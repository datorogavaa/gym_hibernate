package com.crm.hibernate.config;

import com.crm.hibernate.AuditLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Configuration
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public FilterRegistrationBean<AuditLoggingFilter> auditLoggingFilter() {
        log.info("Registering audit logging filter");
        FilterRegistrationBean<AuditLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuditLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}