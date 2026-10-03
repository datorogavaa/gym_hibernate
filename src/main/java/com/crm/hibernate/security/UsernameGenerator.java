package com.crm.hibernate.security;

import com.crm.hibernate.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {
    private final UserRepository userRepository;

    public UsernameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generate(String firstName, String lastName) {
        String base = firstName + "." + lastName;
        long count = userRepository.countByBaseUsername(base);
        return count == 0 ? base : base + count;
    }
}