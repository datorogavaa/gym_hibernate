package com.crm.hibernate.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    void userConstructorAccessorsAndEqualityWork() {
        User user = new User("Jane", "Doe", "jane.doe", "secret", true);

        assertEquals("Jane", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("jane.doe", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertTrue(user.getIsActive());
        assertTrue(user.toString().contains("jane.doe"));

        User sameId = new User();
        sameId.setId(10L);
        user.setId(10L);
        assertEquals(sameId, user);
        assertEquals(user.hashCode(), sameId.hashCode());
        assertNotEquals(user, new User());
        assertNotEquals(user, null);
    }

    @Test
    void trainingTypeConstructorAccessorsAndEqualityWork() {
        TrainingType type = new TrainingType(1L, "FITNESS");

        assertEquals(1L, type.getId());
        assertEquals("FITNESS", type.getTrainingTypeName());
        assertTrue(type.toString().contains("FITNESS"));

        TrainingType sameId = new TrainingType(1L, "YOGA");
        assertEquals(type, sameId);
        assertEquals(type.hashCode(), sameId.hashCode());
    }

    @Test
    void traineeAndTrainerInitializeCollectionsAndExposeRelationships() {
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        User traineeUser = new User();
        TrainingType type = new TrainingType(1L, "FITNESS");

        trainee.setUser(traineeUser);
        trainee.setAddress("Address");
        trainee.setDateOfBirth(new Date());
        trainee.getTrainers().add(trainer);
        trainer.setUser(new User());
        trainer.setSpecialization(type);
        trainer.getTrainees().add(trainee);

        assertSame(traineeUser, trainee.getUser());
        assertEquals("Address", trainee.getAddress());
        assertEquals(1, trainee.getTrainers().size());
        assertEquals(1, trainer.getTrainees().size());
        assertSame(type, trainer.getSpecialization());
        assertTrue(trainee.toString().contains("Address"));
        assertTrue(trainer.toString().contains("FITNESS"));
    }

    @Test
    void trainingExposesAllPropertiesAndUsesIdEquality() {
        Training training = new Training();
        Trainee trainee = new Trainee();
        Trainer trainer = new Trainer();
        TrainingType type = new TrainingType(2L, "YOGA");
        Date date = new Date();

        training.setId(20L);
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName("Morning class");
        training.setTrainingType(type);
        training.setTrainingDate(date);
        training.setTrainingDuration(60.0);

        assertEquals(20L, training.getId());
        assertSame(trainee, training.getTrainee());
        assertSame(trainer, training.getTrainer());
        assertEquals("Morning class", training.getTrainingName());
        assertSame(type, training.getTrainingType());
        assertSame(date, training.getTrainingDate());
        assertEquals(60.0, training.getTrainingDuration());
        assertTrue(training.toString().contains("Morning class"));

        Training sameId = new Training();
        sameId.setId(20L);
        assertEquals(training, sameId);
        assertEquals(training.hashCode(), sameId.hashCode());
    }
}
