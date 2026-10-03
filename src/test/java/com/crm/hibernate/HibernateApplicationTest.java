package com.crm.hibernate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class HibernateApplicationTest {

    @Test
    void mainStartsApplicationContext() {
        assertDoesNotThrow(() -> HibernateApplication.main(new String[0]));
    }
}
