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

    @SuppressWarnings("unchecked")
    public List<Training> findByTraineeCriteria(TraineeTrainingCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);

        Fetch<Training, Trainee> traineeFetch = training.fetch("trainee", JoinType.INNER);
        Fetch<Trainee, User> traineeUserFetch = traineeFetch.fetch("user", JoinType.INNER);

        Fetch<Training, Trainer> trainerFetch = training.fetch("trainer", JoinType.INNER);
        Fetch<Trainer, User> trainerUserFetch = trainerFetch.fetch("user", JoinType.INNER);

        Fetch<Training, TrainingType> typeFetch = training.fetch("trainingType", JoinType.INNER);

        Join<Trainee, User> traineeUser = (Join<Trainee, User>) traineeUserFetch;
        Join<Trainer, User> trainerUser = (Join<Trainer, User>) trainerUserFetch;
        Join<Training, TrainingType> typeJoin = (Join<Training, TrainingType>) typeFetch;

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
        return em.createQuery(cq).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Training> findByTrainerCriteria(TrainerTrainingCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Training> cq = cb.createQuery(Training.class);
        Root<Training> training = cq.from(Training.class);

        Fetch<Training, Trainer> trainerFetch = training.fetch("trainer", JoinType.INNER);
        Fetch<Trainer, User> trainerUserFetch = trainerFetch.fetch("user", JoinType.INNER);

        Fetch<Training, Trainee> traineeFetch = training.fetch("trainee", JoinType.INNER);
        Fetch<Trainee, User> traineeUserFetch = traineeFetch.fetch("user", JoinType.INNER);

        Fetch<Training, TrainingType> typeFetch = training.fetch("trainingType", JoinType.INNER);

        Join<Trainer, User> trainerUser = (Join<Trainer, User>) trainerUserFetch;
        Join<Trainee, User> traineeUser = (Join<Trainee, User>) traineeUserFetch;

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(
                trainerUser.get("username"),
                criteria.getTrainerUsername()
        ));

        if (criteria.getFromDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(
                    training.get("trainingDate"),
                    criteria.getFromDate()
            ));
        }

        if (criteria.getToDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(
                    training.get("trainingDate"),
                    criteria.getToDate()
            ));
        }

        if (criteria.getTraineeName() != null
                && !criteria.getTraineeName().isBlank()) {
            predicates.add(cb.equal(
                    traineeUser.get("firstName"),
                    criteria.getTraineeName()
            ));
        }

        cq.select(training).distinct(true);
        cq.where(predicates.toArray(new Predicate[0]));

        return em.createQuery(cq).getResultList();
    }
}