package com.crm.hibernate.service;

import com.crm.hibernate.dto.TraineeTrainingCriteria;
import com.crm.hibernate.dto.TrainerTrainingCriteria;
import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.Training;
import com.crm.hibernate.repository.TraineeRepository;
import com.crm.hibernate.repository.TrainerRepository;
import com.crm.hibernate.repository.TrainingRepository;
import com.crm.hibernate.security.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);
    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final AuthenticationService authService;

    public TrainingService(TrainingRepository trainingRepository,
                           TraineeRepository traineeRepository,
                           TrainerRepository trainerRepository,
                           AuthenticationService authService) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.authService = authService;
    }

    @Transactional
    public void addTraining(String authUser, String authPass, String traineeUsername, String trainerUsername,
                            String trainingName, Date date, Double duration) {
        authService.authenticate(authUser, authPass);
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
        log.info("Created training '{}' for trainee {} with trainer {}", trainingName, traineeUsername, trainerUsername);
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String authUser, String authPass, TraineeTrainingCriteria criteria) {
        authService.authenticate(authUser, authPass);
        log.debug("Fetching trainings for trainee criteria: {}", criteria.getTraineeUsername());
        return trainingRepository.findByTraineeCriteria(criteria);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String authUser, String authPass, TrainerTrainingCriteria criteria) {
        authService.authenticate(authUser, authPass);
        log.debug("Fetching trainings for trainer criteria: {}", criteria.getTrainerUsername());
        return trainingRepository.findByTrainerCriteria(criteria);
    }
}