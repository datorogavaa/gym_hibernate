package com.crm.hibernate.service;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BruteForceServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BruteForceService bruteForceService;

    @Test
    void loginFailedLocksUserAfterThirdFailure() {
        User user = new User("Bob", "Taylor", "bob.taylor", "secret", true);
        when(userRepository.findByUsername("bob.taylor")).thenReturn(Optional.of(user));

        bruteForceService.loginFailed("bob.taylor");
        bruteForceService.loginFailed("bob.taylor");
        bruteForceService.loginFailed("bob.taylor");

        assertFalse(user.isAccountNonLocked());
        verify(userRepository, org.mockito.Mockito.times(3)).save(user);
    }

    @Test
    void loginSucceededResetsFailureState() {
        User user = new User("Bob", "Taylor", "bob.taylor", "secret", true);
        user.setFailedAttempts(2);
        user.setAccountNonLocked(false);
        user.setLockTime(LocalDateTime.now());
        when(userRepository.findByUsername("bob.taylor")).thenReturn(Optional.of(user));

        bruteForceService.loginSucceeded("bob.taylor");

        assertTrue(user.isAccountNonLocked());
        assertTrue(user.getFailedAttempts() == 0);
        verify(userRepository).save(user);
    }

    @Test
    void expiredLockIsRemoved() {
        User user = new User("Bob", "Taylor", "bob.taylor", "secret", true);
        user.setAccountNonLocked(false);
        user.setFailedAttempts(3);
        user.setLockTime(LocalDateTime.now().minusMinutes(6));

        assertTrue(bruteForceService.unlockWhenTimeExpired(user));
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.getFailedAttempts() == 0);
        verify(userRepository).save(user);
    }
}
