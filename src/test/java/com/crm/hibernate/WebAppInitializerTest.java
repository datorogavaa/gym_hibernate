package com.crm.hibernate;

import com.crm.hibernate.config.AppConfig;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
class WebAppInitializerTest {

    @InjectMocks
    private WebAppInitializer webAppInitializer;

    @Test
    void initializerUsesApplicationAndWebConfiguration() {
        assertArrayEquals(new Class<?>[]{AppConfig.class}, webAppInitializer.getRootConfigClasses());
        assertArrayEquals(new Class<?>[]{WebConfig.class}, webAppInitializer.getServletConfigClasses());
        assertArrayEquals(new String[]{"/"}, webAppInitializer.getServletMappings());
    }

    @Test
    void initializerRegistersAuditLoggingFilter() {
        Filter[] filters = webAppInitializer.getServletFilters();

        assertEquals(1, filters.length);
        assertInstanceOf(AuditLoggingFilter.class, filters[0]);
    }
}
