package com.crm.hibernate.security;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void authenticate(String username, String password) {
        log.debug("Authenticating user: {}", username);
        if (username == null || password == null) {
            log.warn("Authentication failed: null credentials provided");
            throw new SecurityException("Invalid username or password");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user {} not found", username);
                    return new SecurityException("Invalid username or password");
                });

        if (!user.getPassword().equals(password)) {
            log.warn("Authentication failed: invalid password for user {}", username);
            throw new SecurityException("Invalid username or password");
        }
    }

    public boolean validateCredentials(String username, String password) {
        if (username == null || password == null) return false;
        return userRepository.findByUsername(username)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }
}