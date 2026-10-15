package com.crm.hibernate.security;

import com.crm.hibernate.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {
    private final UserRepository userRepository;

    public UsernameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public synchronized String generate(String firstName, String lastName) {
        String base = firstName + "." + lastName;
        long count = userRepository.countByBaseUsername(base);
        if (count == 0 && !userRepository.existsByUsername(base)) {
            return base;
        }

        long suffix = Math.max(1, count);
        String candidate;
        do {
            candidate = base + suffix++;
        } while (userRepository.existsByUsername(candidate));
        return candidate;
    }
}