package com.crm.hibernate.repository;

import com.crm.hibernate.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.Optional;

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
        return em.createQuery("SELECT count(u) FROM User u WHERE u.username LIKE :pattern", Long.class)
                .setParameter("pattern", baseUsername + "%")
                .getSingleResult();
    }
}