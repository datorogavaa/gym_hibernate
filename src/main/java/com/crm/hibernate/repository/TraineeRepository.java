package com.crm.hibernate.repository;

import com.crm.hibernate.entity.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public class TraineeRepository {
    @PersistenceContext
    private EntityManager em;

    public void save(Trainee trainee) {
        if (trainee.getId() == null) em.persist(trainee);
        else em.merge(trainee);
    }

    public Optional<Trainee> findByUsername(String username) {
        try {
            return Optional.of(em.createQuery(
                            "SELECT t FROM Trainee t JOIN FETCH t.user u LEFT JOIN FETCH t.trainers WHERE u.username = :username", Trainee.class)
                    .setParameter("username", username)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public void delete(Trainee trainee) {
        if (trainee == null) {
            return;
        }
        if (em.contains(trainee)) {
            em.remove(trainee);
            return;
        }
        em.remove(em.merge(trainee));
    }
}