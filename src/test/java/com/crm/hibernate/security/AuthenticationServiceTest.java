package com.crm.hibernate.security;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void authenticateSucceedsWhenUsernameAndPasswordMatch() {
        User user = new User("Alice", "Brown", "alice.brown", "secret", true);
        when(userRepository.findByUsername("alice.brown")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authenticationService.authenticate("alice.brown", "secret"));
        verify(userRepository).findByUsername("alice.brown");
    }

    @Test
    void authenticateThrowsWhenPasswordDoesNotMatch() {
        User user = new User("Alice", "Brown", "alice.brown", "secret", true);
        when(userRepository.findByUsername("alice.brown")).thenReturn(Optional.of(user));

        assertThrows(SecurityException.class, () -> authenticationService.authenticate("alice.brown", "wrong-pass"));
    }

    @Test
    void authenticateThrowsForUnknownUser() {
        when(userRepository.findByUsername("missing.user")).thenReturn(Optional.empty());

        assertThrows(SecurityException.class, () -> authenticationService.authenticate("missing.user", "secret"));
    }

    @Test
    void validateCredentialsReturnsTrueWhenPasswordMatches() {
        User user = new User("Alice", "Brown", "alice.brown", "secret", true);
        when(userRepository.findByUsername("alice.brown")).thenReturn(Optional.of(user));

        assertTrue(authenticationService.validateCredentials("alice.brown", "secret"));
        assertFalse(authenticationService.validateCredentials("alice.brown", "wrong-pass"));
    }

    @Test
    void validateCredentialsReturnsFalseWhenCredentialsAreNullOrUnknown() {
        assertFalse(authenticationService.validateCredentials(null, "secret"));
        assertFalse(authenticationService.validateCredentials("missing.user", "secret"));
        assertFalse(authenticationService.validateCredentials("alice.brown", null));
    }

    @Test
    void authenticateRejectsNullCredentials() {
        assertThrows(SecurityException.class, () -> authenticationService.authenticate(null, "secret"));
        assertThrows(SecurityException.class, () -> authenticationService.authenticate("alice.brown", null));
    }
}
