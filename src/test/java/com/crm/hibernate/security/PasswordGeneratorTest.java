package com.crm.hibernate.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorTest {

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void generateReturnsTenCharactersFromAllowedAlphabet() {
        String password = passwordGenerator.generate();

        assertEquals(10, password.length());
        assertTrue(password.chars().allMatch(ch ->
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".indexOf(ch) >= 0));
    }

    @Test
    void generateProducesDifferentPasswordsAcrossCalls() {
        String first = passwordGenerator.generate();
        String second = passwordGenerator.generate();

        assertEquals(10, first.length());
        assertEquals(10, second.length());
    }
}
