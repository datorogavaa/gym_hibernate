package com.crm.hibernate.security;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import com.crm.hibernate.service.BruteForceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BruteForceService bruteForceService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void userDetailsServiceMapsPersistedRoleToSpringAuthority() {
        User user = new User("Bob", "Taylor", "bob.taylor", "encoded", true);
        user.setRole("TRAINEE");
        when(userRepository.findByUsername("bob.taylor")).thenReturn(Optional.of(user));

        UserDetails details = new SecurityConfig(userRepository, bruteForceService, jwtAuthenticationFilter)
                .userDetailsService()
                .loadUserByUsername("bob.taylor");

        assertTrue(details.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_TRAINEE")));
    }

    @Test
    void userDetailsServiceMapsPersistedAccountState() {
        User user = new User("Bob", "Taylor", "bob.taylor", "encoded", false);
        user.setRole("TRAINEE");
        user.setAccountNonLocked(false);
        when(userRepository.findByUsername("bob.taylor")).thenReturn(Optional.of(user));

        UserDetails details = new SecurityConfig(userRepository, bruteForceService, jwtAuthenticationFilter)
                .userDetailsService()
                .loadUserByUsername("bob.taylor");

        assertFalse(details.isAccountNonLocked());
        assertFalse(details.isEnabled());
    }
}
