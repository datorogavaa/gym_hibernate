package com.crm.hibernate.security;

import com.crm.hibernate.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class UsernameGenerator {
    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);
    private final UserRepository userRepository;

    public UsernameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generate(String firstName, String lastName) {
        String base = firstName + "." + lastName;
        long count = userRepository.countByBaseUsername(base);
        String username = count == 0 ? base : base + count;
        log.debug("Generated username candidate: {}", username);
        return username;
    }
}