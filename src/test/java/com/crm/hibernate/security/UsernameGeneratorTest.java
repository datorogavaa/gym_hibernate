package com.crm.hibernate.security;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void generateReturnsBaseUsernameWhenNoUsersExist() {
        when(userRepository.findByUsernameStartingWith("john.doe")).thenReturn(Collections.emptyList());

        String result = usernameGenerator.generate("John", "Doe");

        assertEquals("john.doe", result);
    }

    @Test
    void generateReturnsBaseUsernameWhenBaseNotTaken() {
        User existingUser = mock(User.class);
        when(existingUser.getUsername()).thenReturn("john.doe1");
        when(userRepository.findByUsernameStartingWith("john.doe")).thenReturn(List.of(existingUser));

        String result = usernameGenerator.generate("John", "Doe");

        assertEquals("john.doe", result);
    }

    @Test
    void generateIncrementsMaxSuffixWhenBaseIsTaken() {
        User userBase = mock(User.class);
        User user1 = mock(User.class);
        User user2 = mock(User.class);

        when(userBase.getUsername()).thenReturn("jane.doe");
        when(user1.getUsername()).thenReturn("jane.doe1");
        when(user2.getUsername()).thenReturn("jane.doe2");

        when(userRepository.findByUsernameStartingWith("jane.doe"))
                .thenReturn(List.of(userBase, user1, user2));

        String result = usernameGenerator.generate("Jane", "Doe");

        assertEquals("jane.doe3", result);
    }

    @Test
    void generateIgnoresNonNumericSuffixesWhenFindingMaxSuffix() {
        User userBase = mock(User.class);
        User userWithAlphaSuffix = mock(User.class);
        User user1 = mock(User.class);

        when(userBase.getUsername()).thenReturn("tom.cruise");
        when(userWithAlphaSuffix.getUsername()).thenReturn("tom.cruise.admin");
        when(user1.getUsername()).thenReturn("tom.cruise1");

        when(userRepository.findByUsernameStartingWith("tom.cruise"))
                .thenReturn(List.of(userBase, userWithAlphaSuffix, user1));

        String result = usernameGenerator.generate("Tom", "Cruise");

        assertEquals("tom.cruise2", result);
    }
}