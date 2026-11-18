package com.crm.hibernate.service;

import com.crm.hibernate.dto.TraineeTrainingCriteria;
import com.crm.hibernate.dto.TrainerTrainingCriteria;
import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.Training;
import com.crm.hibernate.integration.client.WorkloadClient;
import com.crm.hibernate.integration.client.TrainerWorkloadRequest;
import com.crm.hibernate.integration.service.WorkloadNotifier;
import com.crm.hibernate.repository.TraineeRepository;
import com.crm.hibernate.repository.TrainerRepository;
import com.crm.hibernate.repository.TrainingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final WorkloadNotifier workloadNotifier;   // NEW
    private final WorkloadClient workloadClient;

    public TrainingService(TrainingRepository trainingRepository,
                           TraineeRepository traineeRepository,
                           TrainerRepository trainerRepository,
                           WorkloadNotifier workloadNotifier,
                           WorkloadClient workloadClient) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.workloadNotifier = workloadNotifier;
        this.workloadClient = workloadClient;
    }

    @Transactional
    public void addTraining(String traineeUsername, String trainerUsername,
                            String trainingName, Date date, Double duration) {
        if (traineeUsername == null || trainerUsername == null || trainingName == null || date == null || duration == null) {
            throw new IllegalArgumentException("All training parameters are required");
        }

        Trainee trainee = traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found"));
        Trainer trainer = trainerRepository.findByUsername(trainerUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found"));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(trainingName);
        training.setTrainingDate(date);
        training.setTrainingDuration(duration);
        training.setTrainingType(trainer.getSpecialization());

        trainingRepository.save(training);

        if (!trainee.getTrainers().contains(trainer)) {
            trainee.getTrainers().add(trainer);
            traineeRepository.save(trainee);
        }

        // NEW: notify the workload microservice
        LocalDate trainingDate = Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        workloadNotifier.notify(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getUser().getIsActive(),
                trainingDate,
                duration,
                TrainerWorkloadRequest.ActionType.ADD);

        log.info("Created training '{}' for trainee {} with trainer {}", trainingName, traineeUsername, trainerUsername);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(TraineeTrainingCriteria criteria) {
        log.debug("Fetching trainings for trainee criteria: {}", criteria.getTraineeUsername());
        return trainingRepository.findByTraineeCriteria(criteria);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(TrainerTrainingCriteria criteria) {
        log.debug("Fetching trainings for trainer criteria: {}", criteria.getTrainerUsername());
        return trainingRepository.findByTrainerCriteria(criteria);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTrainerWorkload(String username) {
        return workloadClient.getWorkload(username);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTrainerMonthlyHours(String username, int year, int month) {
        return workloadClient.getMonthlyHours(username, year, month);
    }
}