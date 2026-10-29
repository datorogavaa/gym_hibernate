package com.crm.hibernate.config;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
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
            assertEquals("jdbc:h2:mem:crm_local", connection.getMetaData().getURL());
            assertEquals("SA", connection.getMetaData().getUserName());
        }
    }

    @Test
    void configUsesHibernateJpaSettings() {
        assertNotNull(entityManagerFactory.getProperties());

        Object ddlAuto = entityManagerFactory.getProperties().get("hibernate.hbm2ddl.auto");
        Object showSql = entityManagerFactory.getProperties().get("hibernate.show_sql");

        assertEquals("create-drop", String.valueOf(ddlAuto));
        assertEquals("true", String.valueOf(showSql));
    }
}