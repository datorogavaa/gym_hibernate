package com.crm.hibernate.config;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = AppConfig.class)
class AppConfigTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void configCreatesDataSourceAndJpaBeans() throws SQLException {
        assertNotNull(dataSource);
        assertNotNull(entityManagerFactory);
        assertNotNull(transactionManager);
        assertTrue(transactionManager instanceof JpaTransactionManager);

        try (Connection connection = dataSource.getConnection()) {
            assertEquals("jdbc:h2:mem:crm_db", connection.getMetaData().getURL());
            assertEquals("SA", connection.getMetaData().getUserName());
        }
    }

    @Test
    void configUsesHibernateJpaSettings() {
        assertNotNull(entityManagerFactory.getProperties());
        assertEquals("create-drop", entityManagerFactory.getProperties().get("hibernate.hbm2ddl.auto"));
        assertEquals("true", entityManagerFactory.getProperties().get("hibernate.show_sql"));
    }
}
