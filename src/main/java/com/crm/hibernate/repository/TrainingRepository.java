package com.crm.hibernate.repository;

import com.crm.hibernate.dto.TraineeTrainingCriteria;
import com.crm.hibernate.dto.TrainerTrainingCriteria;
import com.crm.hibernate.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TrainingRepository {
    @PersistenceContext
    private EntityManager em;

    public void save(Training training) {
        em.persist(training);
    }

    public List<Training> findByTraineeCriteria(TraineeTrainingCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);
        Join<Training, Trainee> traineeJoin = training.join("trainee");
        Join<Trainee, User> traineeUser = traineeJoin.join("user");
        Join<Training, Trainer> trainerJoin = training.join("trainer");
        Join<Trainer, User> trainerUser = trainerJoin.join("user");
        Join<Training, TrainingType> typeJoin = training.join("trainingType");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(traineeUser.get("username"), criteria.getTraineeUsername()));

        if (criteria.getFromDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), criteria.getFromDate()));
        }
        if (criteria.getToDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), criteria.getToDate()));
        }
        if (criteria.getTrainerName() != null && !criteria.getTrainerName().isBlank()) {
            predicates.add(cb.equal(trainerUser.get("firstName"), criteria.getTrainerName()));
        }
        if (criteria.getTrainingType() != null && !criteria.getTrainingType().isBlank()) {
            predicates.add(cb.equal(typeJoin.get("trainingTypeName"), criteria.getTrainingType()));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getResultList();
    }

    public List<Training> findByTrainerCriteria(TrainerTrainingCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);
        Join<Training, Trainer> trainerJoin = training.join("trainer");
        Join<Trainer, User> trainerUser = trainerJoin.join("user");
        Join<Training, Trainee> traineeJoin = training.join("trainee");
        Join<Trainee, User> traineeUser = traineeJoin.join("user");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(trainerUser.get("username"), criteria.getTrainerUsername()));

        if (criteria.getFromDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), criteria.getFromDate()));
        }
        if (criteria.getToDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), criteria.getToDate()));
        }
        if (criteria.getTraineeName() != null && !criteria.getTraineeName().isBlank()) {
            predicates.add(cb.equal(traineeUser.get("firstName"), criteria.getTraineeName()));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getResultList();
    }
}