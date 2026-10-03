package com.crm.hibernate.security;

import com.crm.hibernate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generateReturnsBaseUsernameWhenNoDuplicateExists() {
        when(userRepository.countByBaseUsername("John.Doe")).thenReturn(0L);

        assertEquals("John.Doe", usernameGenerator.generate("John", "Doe"));
    }

    @Test
    void generateAppendsOccurrenceCountWhenUsernameAlreadyExists() {
        when(userRepository.countByBaseUsername("Jane.Doe")).thenReturn(2L);

        assertEquals("Jane.Doe2", usernameGenerator.generate("Jane", "Doe"));
    }

    @Test
    void generateUsesRepositoryCountForDuplicateResolution() {
        when(userRepository.countByBaseUsername("Tom.Cruise")).thenReturn(1L);

        assertEquals("Tom.Cruise1", usernameGenerator.generate("Tom", "Cruise"));
    }
}
