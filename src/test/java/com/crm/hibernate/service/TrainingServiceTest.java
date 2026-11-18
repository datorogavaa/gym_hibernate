package com.crm.hibernate.service;

import com.crm.hibernate.dto.TraineeTrainingCriteria;
import com.crm.hibernate.dto.TrainerTrainingCriteria;
import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.Training;
import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.TraineeRepository;
import com.crm.hibernate.repository.TrainerRepository;
import com.crm.hibernate.repository.TrainingRepository;
import com.crm.hibernate.integration.client.WorkloadClient;
import com.crm.hibernate.integration.service.WorkloadNotifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private WorkloadNotifier workloadNotifier;

    @Mock
    private WorkloadClient workloadClient;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void addTrainingCreatesTrainingAndLinksTrainerToTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("Alice", "Brown", "alice.brown", "secret", true));
        Trainer trainer = new Trainer();
        trainer.setUser(new User("Bob", "Jones", "bob.jones", "secret", true));
        trainer.setSpecialization(new TrainingType(1L, "Java"));
        Date date = new Date();

        when(traineeRepository.findByUsername("alice.brown")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("bob.jones")).thenReturn(Optional.of(trainer));

        trainingService.addTraining("alice.brown", "bob.jones",
                "Java Bootcamp", date, 12.5);

        assertTrue(trainee.getTrainers().contains(trainer));
        verify(trainingRepository).save(any(Training.class));
        verify(traineeRepository).save(trainee);
        verify(workloadNotifier).notify(
                eq("bob.jones"), eq("Bob"), eq("Jones"), eq(true),
                any(java.time.LocalDate.class), eq(12.5),
                eq(com.crm.hibernate.integration.client.TrainerWorkloadRequest.ActionType.ADD));
    }

    @Test
    void getTraineeTrainingsDelegatesToRepository() {
        List<Training> trainings = List.of(new Training());
        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria("alice.brown", null, null, null, null);
        when(trainingRepository.findByTraineeCriteria(criteria)).thenReturn(trainings);

        List<Training> result = trainingService.getTraineeTrainings(criteria);

        assertEquals(trainings, result);
    }

    @Test
    void getTrainerTrainingsDelegatesToRepository() {
        List<Training> trainings = List.of(new Training());
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria("bob.jones", null, null, null);
        when(trainingRepository.findByTrainerCriteria(criteria)).thenReturn(trainings);

        List<Training> result = trainingService.getTrainerTrainings(criteria);

        assertEquals(trainings, result);
    }

    @Test
    void getTrainerWorkloadDelegatesToWorkloadClient() {
        Map<String, Object> workload = Map.of("trainerUsername", "bob.jones");
        when(workloadClient.getWorkload("bob.jones")).thenReturn(workload);

        assertEquals(workload, trainingService.getTrainerWorkload("bob.jones"));
    }

    @Test
    void getTrainerMonthlyHoursDelegatesToWorkloadClient() {
        Map<String, Object> monthly = Map.of("total", 12L);
        when(workloadClient.getMonthlyHours("bob.jones", 2026, 11)).thenReturn(monthly);

        assertEquals(monthly,
                trainingService.getTrainerMonthlyHours("bob.jones", 2026, 11));
    }

    @Test
    void addTrainingDoesNotDuplicateTrainerInTraineeList() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("Alice", "Brown", "alice.brown", "secret", true));
        Trainer existingTrainer = new Trainer();
        existingTrainer.setId(7L);
        trainee.getTrainers().add(existingTrainer);

        Trainer trainer = new Trainer();
        trainer.setId(7L);
        trainer.setUser(new User("Bob", "Jones", "bob.jones", "secret", true));
        trainer.setSpecialization(new TrainingType(1L, "Java"));
        Date date = new Date();

        when(traineeRepository.findByUsername("alice.brown")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("bob.jones")).thenReturn(Optional.of(trainer));

        trainingService.addTraining("alice.brown", "bob.jones", "Java Bootcamp", date, 12.5);

        assertEquals(1, trainee.getTrainers().size());
    }

    @Test
    void addTrainingRejectsMissingParameters() {
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining(null, "bob.jones", "Java", new Date(), 10.0));
        assertThrows(IllegalArgumentException.class,
                () -> trainingService.addTraining("alice.brown", "bob.jones", null, new Date(), 10.0));
    }
}
