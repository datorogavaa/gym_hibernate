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
        if (trainer.getId() == null) em.persist(trainer);
        else em.merge(trainer);
    }

    public Optional<Trainer> findByUsername(String username) {
        try {
            return Optional.of(em.createQuery(
                            "SELECT tr FROM Trainer tr JOIN FETCH tr.user u JOIN FETCH tr.specialization WHERE u.username = :username", Trainer.class)
                    .setParameter("username", username)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
        return em.createQuery(
                        "SELECT tr FROM Trainer tr WHERE tr NOT IN " +
                                "(SELECT ttr FROM Trainee t JOIN t.trainers ttr JOIN t.user u WHERE u.username = :traineeUsername)", Trainer.class)
                .setParameter("traineeUsername", traineeUsername)
                .getResultList();
    }

    public List<Trainer> findByUsernames(Set<String> usernames) {
        if (usernames == null || usernames.isEmpty()) return new ArrayList<>();
        return em.createQuery("SELECT tr FROM Trainer tr JOIN tr.user u WHERE u.username IN :usernames", Trainer.class)
                .setParameter("usernames", usernames)
                .getResultList();
    }
}