package com.crm.hibernate.security;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UsernameGenerator {

    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);
    private final UserRepository userRepository;

    public UsernameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generate(String firstName, String lastName) {
        String base = (firstName + "." + lastName).toLowerCase();
        List<User> existingUsers = userRepository.findByUsernameStartingWith(base);

        if (existingUsers.isEmpty()) {
            log.debug("Generated username candidate: {}", base);
            return base;
        }

        Set<String> existingUsernames = existingUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());

        if (!existingUsernames.contains(base)) {
            return base;
        }

        long maxSuffix = 0;
        for (String username : existingUsernames) {
            String suffixStr = username.substring(base.length());
            if (!suffixStr.isEmpty()) {
                try {
                    long suffix = Long.parseLong(suffixStr);
                    if (suffix > maxSuffix) {
                        maxSuffix = suffix;
                    }
                } catch (NumberFormatException ignored) {

                }
            }
        }

        String username = base + (maxSuffix + 1);
        log.debug("Generated username candidate: {}", username);
        return username;
    }
}