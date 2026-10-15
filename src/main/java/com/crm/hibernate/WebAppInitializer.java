package com.crm.hibernate;

import com.crm.hibernate.config.AppConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    private static final Logger log = LoggerFactory.getLogger(WebAppInitializer.class);

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?>[] { AppConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { WebConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    protected Filter[] getServletFilters() {
        log.info("Registering web application filters");
        return new Filter[] { new AuditLoggingFilter() };
    }
}