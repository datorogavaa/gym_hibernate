package com.crm.hibernate.security;

import com.crm.hibernate.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class UsernameGenerator {
    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);
    private final UserRepository userRepository;

    public UsernameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generate(String firstName, String lastName) {
        String base = firstName + "." + lastName;

        List<String> existingUsernames = userRepository.findUsernamesByBaseUsername(base);
        Set<String> existing = (existingUsernames != null) ? new HashSet<>(existingUsernames) : new HashSet<>();

        if (!existing.contains(base)) {
            return logCandidate(base);
        }

        long suffix = 1;
        while (existing.contains(base + suffix)) {
            suffix++;
        }

        return logCandidate(base + suffix);
    }

    private String logCandidate(String username) {
        log.debug("Generated username candidate: {}", username);
        return username;
    }
}