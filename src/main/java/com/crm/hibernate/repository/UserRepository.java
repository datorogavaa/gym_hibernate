package com.crm.hibernate.repository;

import com.crm.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public class UserRepository {
    @PersistenceContext
    private EntityManager em;

    public void save(User user) {
        if (user.getId() == null) em.persist(user);
        else em.merge(user);
    }

    public Optional<User> findByUsername(String username) {
        try {
            return Optional.of(em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public long countByBaseUsername(String baseUsername) {
        List<String> usernames = em.createQuery("SELECT u.username FROM User u WHERE u.username LIKE :pattern", String.class)
                .setParameter("pattern", baseUsername + "%")
                .getResultList();
        return usernames.stream()
                .filter(username -> username.matches(java.util.regex.Pattern.quote(baseUsername) + "\\d*"))
                .count();
    }

    public boolean existsByUsername(String username) {
        return !em.createQuery("SELECT u.id FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }
}