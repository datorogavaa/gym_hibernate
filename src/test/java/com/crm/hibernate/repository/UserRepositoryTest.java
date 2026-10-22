package com.crm.hibernate.repository;

import com.crm.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<User> userQuery;

    @Mock
    private TypedQuery<String> stringQuery;

    @InjectMocks
    private UserRepository userRepository;

    @Test
    void savePersistsNewEntityWhenIdIsNull() {
        User john = new User("Repo", "User", "repo.user.new", "secret", true);

        userRepository.save(john);

        verify(em).persist(john);
    }

    @Test
    void saveMergesExistingEntityWhenIdIsPresent() {
        User john = new User("Repo", "Update", "repo.user.update", "secret", true);
        john.setId(1000L);

        userRepository.save(john);

        verify(em).merge(john);
    }

    @Test
    void findByUsernameReturnsUserWhenExists() {
        User john = new User("Repo", "Lookup", "repo.user.lookup", "secret", true);

        when(em.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setParameter("username", "repo.user.lookup")).thenReturn(userQuery);
        when(userQuery.getSingleResult()).thenReturn(john);

        Optional<User> found = userRepository.findByUsername("repo.user.lookup");

        assertTrue(found.isPresent());
        assertEquals("repo.user.lookup", found.get().getUsername());
    }

    @Test
    void findByUsernameReturnsEmptyWhenMissing() {
        when(em.createQuery(anyString(), eq(User.class))).thenReturn(userQuery);
        when(userQuery.setParameter("username", "unknown.user")).thenReturn(userQuery);
        when(userQuery.getSingleResult()).thenThrow(new NoResultException());

        Optional<User> found = userRepository.findByUsername("unknown.user");

        assertTrue(found.isEmpty());
    }

    @Test
    void findUsernamesByBaseUsernameReturnsMatchingUsernames() {
        List<String> mockUsernames = List.of("John.Doe", "John.Doe1");

        when(em.createQuery(anyString(), eq(String.class))).thenReturn(stringQuery);
        when(stringQuery.setParameter("base", "John.Doe")).thenReturn(stringQuery);
        when(stringQuery.setParameter("pattern", "John.Doe%")).thenReturn(stringQuery);
        when(stringQuery.getResultList()).thenReturn(mockUsernames);

        List<String> usernames = userRepository.findUsernamesByBaseUsername("John.Doe");

        assertEquals(2, usernames.size());
        assertTrue(usernames.contains("John.Doe"));
        assertTrue(usernames.contains("John.Doe1"));
    }
}