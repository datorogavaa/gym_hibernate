package com.crm.hibernate.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {
        "com.crm.hibernate.repository",
        "com.crm.hibernate.service",
        "com.crm.hibernate.security",
        "com.crm.hibernate.exception"
})
@PropertySource("classpath:application.properties")
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        log.info("Configuring application data source");
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty("spring.datasource.driverClassName"));
        dataSource.setUrl(env.getRequiredProperty("spring.datasource.url"));
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
    @DependsOn("entityManagerFactory")
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        log.info("Configuring database data initializer");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("data.sql"));

        populator.setContinueOnError(true);

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        log.info("Configuring JPA transaction manager");
        return new JpaTransactionManager(entityManagerFactory);
    }
}