package com.crm.hibernate.repository;

import com.crm.hibernate.entity.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class TrainerRepository {

    @PersistenceContext
    private EntityManager em;

    public void save(Trainer trainer) {
        if (trainer.getId() == null) {
            em.persist(trainer);
        } else {
            em.merge(trainer);
        }
    }

    public Optional<Trainer> findByUsername(String username) {
        try {
            return Optional.of(em.createQuery(
                            "SELECT DISTINCT tr FROM Trainer tr " +
                                    "JOIN FETCH tr.user u " +
                                    "JOIN FETCH tr.specialization " +
                                    "LEFT JOIN FETCH tr.trainees trainees " +
                                    "LEFT JOIN FETCH trainees.user " +
                                    "WHERE u.username = :username",
                            Trainer.class)
                    .setParameter("username", username)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
        return em.createQuery(
                "SELECT DISTINCT tr FROM Trainer tr JOIN FETCH tr.user " +
                        "WHERE NOT EXISTS (" +
                        "   SELECT 1 FROM Trainee t JOIN t.trainers ttr " +
                        "   JOIN t.user traineeUser WHERE ttr.id = tr.id AND traineeUser.username = :traineeUsername" +
                        ")",
                Trainer.class
        ).setParameter("traineeUsername", traineeUsername).getResultList();
    }

    public List<Trainer> findByUsernames(Set<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return new ArrayList<>();
        }
        return em.createQuery(
                        "SELECT DISTINCT tr FROM Trainer tr " +
                                "JOIN FETCH tr.user u " + // <-- ჩაემატა FETCH
                                "WHERE u.username IN :usernames",
                        Trainer.class)
                .setParameter("usernames", usernames)
                .getResultList();
    }
}