package com.crm.hibernate.config;

import com.crm.hibernate.AuditLoggingFilter;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

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