package com.crm.hibernate.service;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import com.crm.hibernate.security.AuthenticationService;
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

    @Mock
    private AuthenticationService authService;

    @InjectMocks
    private UserService userService;

    @Test
    void changePasswordUpdatesUserAndPersistsChanges() {
        User user = new User("John", "Doe", "john.doe", "oldPass", true);
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        userService.changePassword("admin", "adminPass", "john.doe", "newSecret");

        assertEquals("newSecret", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordRejectsBlankNewPassword() {
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("admin", "adminPass", "john.doe", "   "));
        verifyNoInteractions(userRepository);
    }

    @Test
    void changePasswordThrowsWhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing.user")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> userService.changePassword("admin", "adminPass", "missing.user", "newSecret"));
    }

    @Test
    void changePasswordAuthenticatesBeforeUpdatingPassword() {
        User user = new User("John", "Doe", "john.doe", "oldPass", true);
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        userService.changePassword("admin", "adminPass", "john.doe", "newSecret");

        verify(authService).authenticate("admin", "adminPass");
    }
}
