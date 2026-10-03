package com.crm.hibernate.repository;

import com.crm.hibernate.config.AppConfig;
import com.crm.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(classes = AppConfig.class)
@Transactional
class UserRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    private void resetUsersIdentity() {
        em.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1000").executeUpdate();
    }

    @Test
    void savePersistsNewEntityWhenIdIsNull() {
        resetUsersIdentity();
        User john = new User("Repo", "User", "repo.user.new", "secret", true);

        userRepository.save(john);
        em.flush();
        em.clear();

        assertTrue(userRepository.findByUsername("repo.user.new").isPresent());
    }

    @Test
    void saveMergesExistingEntityWhenIdIsPresent() {
        resetUsersIdentity();
        User john = new User("Repo", "Update", "repo.user.update", "secret", true);
        userRepository.save(john);
        em.flush();
        em.clear();

        User loaded = userRepository.findByUsername("repo.user.update").orElseThrow();
        loaded.setLastName("Updated");
        userRepository.save(loaded);
        em.flush();
        em.clear();

        assertEquals("Updated", userRepository.findByUsername("repo.user.update").orElseThrow().getLastName());
    }

    @Test
    void findByUsernameReturnsUserWhenExistsAndEmptyWhenMissing() {
        resetUsersIdentity();
        User john = new User("Repo", "Lookup", "repo.user.lookup", "secret", true);
        userRepository.save(john);
        em.flush();
        em.clear();

        Optional<User> found = userRepository.findByUsername("repo.user.lookup");
        assertEquals("repo.user.lookup", found.orElseThrow().getUsername());
        assertTrue(userRepository.findByUsername("unknown.user").isEmpty());
    }

    @Test
    void countByBaseUsernameCountsMatchingPrefixOnly() {
        resetUsersIdentity();
        User john = new User("Repo", "Count", "repo.count.one", "secret", true);
        User jane = new User("Repo", "Count", "repo.count.two", "secret", true);
        User alice = new User("Repo", "Other", "repo.other.user", "secret", true);

        userRepository.save(john);
        userRepository.save(jane);
        userRepository.save(alice);
        em.flush();
        em.clear();

        assertEquals(2L, userRepository.countByBaseUsername("repo.count"));
        assertEquals(0L, userRepository.countByBaseUsername("missing.base"));
        assertFalse(userRepository.findByUsername("repo.other.user").isEmpty());
    }
}