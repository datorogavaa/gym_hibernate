package com.crm.hibernate.security;

import com.crm.hibernate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generateReturnsBaseUsernameWhenNoDuplicatesExist() {
        when(userRepository.findUsernamesByBaseUsername("John.Doe"))
                .thenReturn(Collections.emptyList());

        String result = usernameGenerator.generate("John", "Doe");

        assertEquals("John.Doe", result);
    }

    @Test
    void generateAppendsSuffixOneWhenBaseUsernameExists() {
        when(userRepository.findUsernamesByBaseUsername("Jane.Doe"))
                .thenReturn(List.of("Jane.Doe"));

        String result = usernameGenerator.generate("Jane", "Doe");

        assertEquals("Jane.Doe1", result);
    }

    @Test
    void generateFillsGapWhenUserHasBeenDeleted() {
        // Simulates Jane.Doe1 being deleted, leaving Jane.Doe and Jane.Doe2
        when(userRepository.findUsernamesByBaseUsername("Jane.Doe"))
                .thenReturn(List.of("Jane.Doe", "Jane.Doe2"));

        String result = usernameGenerator.generate("Jane", "Doe");

        assertEquals("Jane.Doe1", result);
    }

    @Test
    void generateIncrementsToNextAvailableSuffixWhenMultipleExist() {
        when(userRepository.findUsernamesByBaseUsername("Tom.Cruise"))
                .thenReturn(List.of("Tom.Cruise", "Tom.Cruise1", "Tom.Cruise2"));

        String result = usernameGenerator.generate("Tom", "Cruise");

        assertEquals("Tom.Cruise3", result);
    }

    @Test
    void generateReturnsBaseUsernameWhenRepositoryReturnsNull() {
        when(userRepository.findUsernamesByBaseUsername("Alice.Smith"))
                .thenReturn(null);

        String result = usernameGenerator.generate("Alice", "Smith");

        assertEquals("Alice.Smith", result);
    }
}