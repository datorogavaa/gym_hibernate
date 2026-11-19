package com.crm.hibernate.integration.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class TransactionLoggingFilterTest {

    @Test
    void sanitizesPasswordOnly() {
        String sanitized = TransactionLoggingFilter.sanitizePassword(
                "{\"username\":\"bob.taylor\",\"password\":\"Pass12345!\",\"token\":\"jwt-value\"}");

        assertEquals("{\"username\":\"bob.taylor\",\"password\":\"*****\",\"token\":\"jwt-value\"}", sanitized);
        assertFalse(sanitized.contains("Pass12345!"));
    }

    @Test
    void sanitizesPasswordCaseInsensitively() {
        String sanitized = TransactionLoggingFilter.sanitizePassword(
                "{\"PASSWORD\":\"secret\",\"username\":\"bob.taylor\"}");

        assertEquals("{\"PASSWORD\":\"*****\",\"username\":\"bob.taylor\"}", sanitized);
    }
}
