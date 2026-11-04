package com.crm.hibernate.service;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void changePassword(String targetUsername, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        User user = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + targetUsername));
        user.setPassword(newPassword);
        userRepository.save(user);
        log.info("Password successfully updated for user: {}", targetUsername);
    }

}