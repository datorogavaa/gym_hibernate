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
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.crm.hibernate.repository")
@ComponentScan(basePackages = {
        "com.crm.hibernate.service",
        "com.crm.hibernate.security",
        "com.crm.hibernate.observability"
})
@PropertySource("classpath:application.properties")
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public FilterRegistrationBean<AuditLoggingFilter> auditLoggingFilter() {
        log.info("Registering audit logging filter");
        FilterRegistrationBean<AuditLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuditLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public DataSource dataSource() {
        log.info("Configuring application data source");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driverClassName", "org.h2.Driver"));
        dataSource.setUrl(env.getProperty("spring.datasource.url", "jdbc:h2:mem:crm_db;DB_CLOSE_DELAY=-1"));
        dataSource.setUsername(env.getProperty("spring.datasource.username", "sa"));
        dataSource.setPassword(env.getProperty("spring.datasource.password", ""));
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        log.info("Configuring Hibernate entity manager factory");
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.crm.hibernate.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Properties jpaProperties = new Properties();

        jpaProperties.setProperty("hibernate.hbm2ddl.auto",
                env.getProperty("spring.jpa.hibernate.ddl-auto", "create-drop"));


        jpaProperties.setProperty("hibernate.show_sql",
                env.getProperty("spring.jpa.show-sql", "true"));
        jpaProperties.setProperty("hibernate.format_sql",
                env.getProperty("spring.jpa.properties.hibernate.format_sql", "true"));

        em.setJpaProperties(jpaProperties);
        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        log.info("Configuring JPA transaction manager");
        return new JpaTransactionManager(entityManagerFactory);
    }
}