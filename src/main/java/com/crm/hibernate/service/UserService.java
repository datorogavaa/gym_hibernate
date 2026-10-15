package com.crm.hibernate.service;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import com.crm.hibernate.security.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final AuthenticationService authService;

    public UserService(UserRepository userRepository, AuthenticationService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional
    public void changePassword(String authUser, String authPass, String targetUsername, String newPassword) {
        authService.authenticate(authUser, authPass);
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        User user = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + targetUsername));
        user.setPassword(newPassword);
        userRepository.save(user);
        log.info("Password successfully updated for user: {}", targetUsername);
    }

    @Transactional
    public void activateDeactivate(String authUser, String authPass, String targetUsername) {
        authService.authenticate(authUser, authPass);
        validateUsername(targetUsername);

        User user = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + targetUsername));

        boolean nextState = !Boolean.TRUE.equals(user.getIsActive());
        user.setIsActive(nextState);
        userRepository.save(user);

        log.info("Toggled user {} active state to {}", targetUsername, nextState);
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
    }
}