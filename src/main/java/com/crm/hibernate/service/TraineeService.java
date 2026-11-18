package com.crm.hibernate.service;

import com.crm.hibernate.dto.TrainerSummaryDto;
import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.Training;
import com.crm.hibernate.entity.User;
import com.crm.hibernate.integration.client.TrainerWorkloadRequest;
import com.crm.hibernate.integration.service.WorkloadNotifier;
import com.crm.hibernate.repository.TraineeRepository;
import com.crm.hibernate.repository.TrainerRepository;
import com.crm.hibernate.repository.TrainingRepository;
import com.crm.hibernate.security.PasswordGenerator;
import com.crm.hibernate.security.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UsernameGenerator usernameGen;
    private final PasswordGenerator passwordGen;
    private final PasswordEncoder passwordEncoder;
    private final TrainingRepository trainingRepository;
    private final WorkloadNotifier workloadNotifier;


    public TraineeService(TraineeRepository traineeRepository,
                          TrainerRepository trainerRepository,
                          UsernameGenerator usernameGen,
                          PasswordGenerator passwordGen,
                          PasswordEncoder passwordEncoder,
                          TrainingRepository trainingRepository,
                          WorkloadNotifier workloadNotifier) {
        this.trainingRepository = trainingRepository;
        this.workloadNotifier = workloadNotifier;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.usernameGen = usernameGen;
        this.passwordGen = passwordGen;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Trainee createProfile(String firstName, String lastName, Date dob, String address) {
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");

        String username = usernameGen.generate(firstName, lastName);
        String password = passwordGen.generate();
        User newUser = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password(passwordEncoder.encode(password))
                .isActive(true)
                .role("TRAINEE")
                .generatedPassword(password)
                .build();
        Trainee trainee = new Trainee();
        trainee.setUser(newUser);
        trainee.setDateOfBirth(dob);
        trainee.setAddress(address);

        traineeRepository.save(trainee);
        log.info("Created Trainee profile: {}", username);
        return trainee;
    }

    @Transactional(readOnly = true)
    public Trainee selectProfile(String targetUsername) {
        validateRequired(targetUsername, "Username");
        return traineeRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found"));
    }

    @Transactional
    public Trainee updateProfile(String targetUsername,
                                 String firstName, String lastName, Date dob, String address, boolean isActive) {
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");

        Trainee trainee = selectProfile(targetUsername);
        trainee.getUser().setFirstName(firstName);
        trainee.getUser().setLastName(lastName);
        trainee.getUser().setIsActive(isActive);
        trainee.setDateOfBirth(dob);
        trainee.setAddress(address);

        traineeRepository.save(trainee);
        log.info("Updated Trainee profile: {}", targetUsername);
        return trainee;
    }

    @Transactional
    public void activateDeactivate(String targetUsername, boolean isActive) {
        Trainee trainee = selectProfile(targetUsername);
        trainee.getUser().setIsActive(isActive);
        traineeRepository.save(trainee);

        log.info("Updated Trainee {} isActive status to {}", targetUsername, isActive);
    }


    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        validateRequired(traineeUsername, "Trainee username");
        return trainerRepository.findNotAssignedToTrainee(traineeUsername);
    }

    @Transactional
    public void deleteProfile(String username) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found"));

        // 1. copy the trainings BEFORE deleting (they are gone afterward)
        List<Training> trainings = new ArrayList<>(trainingRepository.findByTrainee(trainee));
        // if you have no such repository method, use: new ArrayList<>(trainee.getTrainings())

        // 2. clear associations before deleting
        trainee.getTrainers().clear();
        traineeRepository.delete(trainee);

        // 3. notify the workload service for each removed training
        for (Training t : trainings) {
            Trainer trainer = t.getTrainer();
            LocalDate date = Instant.ofEpochMilli(t.getTrainingDate().getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            workloadNotifier.notify(
                    trainer.getUser().getUsername(),
                    trainer.getUser().getFirstName(),
                    trainer.getUser().getLastName(),
                    trainer.getUser().getIsActive(),          // your real getter name
                    date,
                    t.getTrainingDuration(),
                    TrainerWorkloadRequest.ActionType.DELETE);
        }
    }

    @Transactional
    public List<TrainerSummaryDto> updateTrainersList(String traineeUsername,
                                                      Set<String> trainerUsernames) {
        Trainee trainee = selectProfile(targetUsernameOrFallback(traineeUsername));

        List<Trainer> trainers = trainerRepository.findByUsernames(trainerUsernames);
        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);
        traineeRepository.save(trainee);

        log.info("Updated trainers for trainee {}", traineeUsername);

        return trainers.stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization() != null ? trainer.getSpecialization().getTrainingTypeName() : null
                ))
                .collect(Collectors.toList());
    }

    private void validateRequired(String val, String field) {
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private String targetUsernameOrFallback(String name) {
        validateRequired(name, "Trainee username");
        return name;
    }
}