package com.crm.hibernate.repository;

import com.crm.hibernate.config.AppConfig;
import com.crm.hibernate.dto.TraineeTrainingCriteria;
import com.crm.hibernate.dto.TrainerTrainingCriteria;
import com.crm.hibernate.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringJUnitConfig(classes = AppConfig.class)
@Transactional
class TrainingRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private TrainingRepository trainingRepository;

    private void resetIdentitySequences() {
        em.createNativeQuery("ALTER TABLE training_types ALTER COLUMN id RESTART WITH 1000").executeUpdate();
        em.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1000").executeUpdate();
        em.createNativeQuery("ALTER TABLE trainers ALTER COLUMN id RESTART WITH 1000").executeUpdate();
        em.createNativeQuery("ALTER TABLE trainees ALTER COLUMN id RESTART WITH 1000").executeUpdate();
        em.createNativeQuery("ALTER TABLE trainings ALTER COLUMN id RESTART WITH 1000").executeUpdate();
    }

    @Test
    void savePersistsTrainingAndCriteriaQueriesReturnResults() {
        resetIdentitySequences();
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Java");
        User traineeUser = new User("Repo", "Trainee", "repo.trainee.training", "secret", true);
        User trainerUser = new User("Repo", "Trainer", "repo.trainer.training", "secret", true);

        Trainee trainee = new Trainee();
        trainee.setUser(traineeUser);
        Trainer trainer = new Trainer();
        trainer.setUser(trainerUser);
        trainer.setSpecialization(type);

        em.persist(type);
        em.persist(traineeUser);
        em.persist(trainerUser);
        em.persist(trainee);
        em.persist(trainer);

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Java Bootcamp");
        training.setTrainingType(type);
        training.setTrainingDate(new Date());
        training.setTrainingDuration(10.5);

        trainingRepository.save(training);
        em.flush();
        em.clear();

        TraineeTrainingCriteria traineeCriteria = new TraineeTrainingCriteria("repo.trainee.training", null, null, null, "Java");
        TrainerTrainingCriteria trainerCriteria = new TrainerTrainingCriteria("repo.trainer.training", null, null, null);

        List<Training> byTrainee = trainingRepository.findByTraineeCriteria(traineeCriteria);
        List<Training> byTrainer = trainingRepository.findByTrainerCriteria(trainerCriteria);

        assertFalse(byTrainee.isEmpty());
        assertFalse(byTrainer.isEmpty());
        assertEquals("Java Bootcamp", byTrainee.get(0).getTrainingName());
    }
}