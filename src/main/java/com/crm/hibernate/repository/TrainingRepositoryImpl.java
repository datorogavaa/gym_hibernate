package com.crm.hibernate.repository;

import com.crm.hibernate.dto.TraineeTrainingCriteria;
import com.crm.hibernate.dto.TrainerTrainingCriteria;
import com.crm.hibernate.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;

import java.util.ArrayList;
import java.util.List;

public class TrainingRepositoryImpl implements TrainingRepositoryCustom {
    private final EntityManager entityManager;

    public TrainingRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Training> findByTraineeCriteria(TraineeTrainingCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);

        Fetch<Training, Trainee> traineeFetch = training.fetch("trainee", JoinType.INNER);
        traineeFetch.fetch("user", JoinType.INNER);

        Fetch<Training, Trainer> trainerFetch = training.fetch("trainer", JoinType.INNER);
        trainerFetch.fetch("user", JoinType.INNER);

        training.fetch("trainingType", JoinType.INNER);

        Join<Training, Trainee> traineeJoin = training.join("trainee", JoinType.INNER);
        Join<Trainee, User> traineeUser = traineeJoin.join("user", JoinType.INNER);

        Join<Training, Trainer> trainerJoin = training.join("trainer", JoinType.INNER);
        Join<Trainer, User> trainerUser = trainerJoin.join("user", JoinType.INNER);

        Join<Training, TrainingType> typeJoin = training.join("trainingType", JoinType.INNER);

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

        cq.select(training).distinct(true);
        cq.where(predicates.toArray(new Predicate[0]));

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Training> findByTrainerCriteria(TrainerTrainingCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);
        training.fetch("trainee", JoinType.INNER).fetch("user", JoinType.INNER);
        training.fetch("trainingType", JoinType.INNER);
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
        cq.select(training).distinct(true);
        cq.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getResultList();
    }
}
