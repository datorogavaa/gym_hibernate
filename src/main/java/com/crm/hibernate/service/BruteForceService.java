package com.crm.hibernate.service;

import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class BruteForceService {

    private final UserRepository userRepository;

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MINUTES = 5;

    public BruteForceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void loginFailed(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (!user.isAccountNonLocked()) {
                if (unlockWhenTimeExpired(user)) {
                    user.setFailedAttempts(1);
                    user.setAccountNonLocked(true);
                    userRepository.save(user);
                    return;
                }
                return;
            }

            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);

            if (attempts >= MAX_ATTEMPTS) {
                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());
            }
            userRepository.save(user);
        });
    }

    public void loginSucceeded(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getFailedAttempts() > 0 || !user.isAccountNonLocked()) {
                user.setFailedAttempts(0);
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                userRepository.save(user);
            }
        });
    }

    public boolean unlockWhenTimeExpired(User user) {
        if (user.getLockTime() != null) {
            LocalDateTime lockExpireTime = user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES);
            if (LocalDateTime.now().isAfter(lockExpireTime)) {
                user.setAccountNonLocked(true);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}