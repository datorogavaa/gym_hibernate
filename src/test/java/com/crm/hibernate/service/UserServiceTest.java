package com.crm.hibernate.service;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void changePasswordUpdatesUserAndPersistsChanges() {
        User user = new User("John", "Doe", "john.doe", "oldPass", true);
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        userService.changePassword("john.doe", "newSecret");

        assertEquals("newSecret", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordRejectsBlankNewPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("john.doe", "   "));
        verifyNoInteractions(userRepository);
    }

    @Test
    void changePasswordThrowsWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing.user")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.changePassword("missing.user", "newSecret"));
    }

}
