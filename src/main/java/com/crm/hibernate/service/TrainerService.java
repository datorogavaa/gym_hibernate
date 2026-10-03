package com.crm.hibernate.service;

import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.TrainerRepository;
import com.crm.hibernate.repository.TrainingTypeRepository;
import com.crm.hibernate.security.AuthenticationService;
import com.crm.hibernate.security.PasswordGenerator;
import com.crm.hibernate.security.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UsernameGenerator usernameGen;
    private final PasswordGenerator passwordGen;
    private final AuthenticationService authService;

    public TrainerService(TrainerRepository trainerRepository,
                          TrainingTypeRepository trainingTypeRepository,
                          UsernameGenerator usernameGen,
                          PasswordGenerator passwordGen,
                          AuthenticationService authService) {
        this.trainerRepository = trainerRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.usernameGen = usernameGen;
        this.passwordGen = passwordGen;
        this.authService = authService;
    }

    @Transactional
    public Trainer createProfile(String firstName, String lastName, Long specializationId) {
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");
        if (specializationId == null) throw new IllegalArgumentException("Specialization ID is required");

        TrainingType type = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found: " + specializationId));

        String username = usernameGen.generate(firstName, lastName);
        String password = passwordGen.generate();
        User user = new User(firstName, lastName, username, password, true);

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(type);

        trainerRepository.save(trainer);
        log.info("Created Trainer profile: {}", username);
        return trainer;
    }

    @Transactional(readOnly = true)
    public Trainer selectProfile(String authUser, String authPass, String targetUsername) {
        authService.authenticate(authUser, authPass);
        validateRequired(targetUsername, "Username");
        return trainerRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainer not found"));
    }

    @Transactional
    public Trainer updateProfile(String authUser, String authPass, String targetUsername,
                                 String firstName, String lastName, Long specializationId, boolean isActive) {
        authService.authenticate(authUser, authPass);
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");
        if (specializationId == null) throw new IllegalArgumentException("Specialization ID is required");

        Trainer trainer = selectProfile(authUser, authPass, targetUsername);
        TrainingType type = trainingTypeRepository.findById(specializationId)
                .orElseThrow(() -> new IllegalArgumentException("Specialization not found"));

        trainer.getUser().setFirstName(firstName);
        trainer.getUser().setLastName(lastName);
        trainer.getUser().setIsActive(isActive);
        trainer.setSpecialization(type);

        trainerRepository.save(trainer);
        log.info("Updated Trainer profile: {}", targetUsername);
        return trainer;
    }

    @Transactional
    public void activateDeactivate(String authUser, String authPass, String targetUsername) {
        authService.authenticate(authUser, authPass);
        Trainer trainer = selectProfile(authUser, authPass, targetUsername);
        boolean state = !trainer.getUser().getIsActive();
        trainer.getUser().setIsActive(state);
        trainerRepository.save(trainer);
        log.info("Toggled Trainer {} isActive to {}", targetUsername, state);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String authUser, String authPass, String traineeUsername) {
        authService.authenticate(authUser, authPass);
        validateRequired(traineeUsername, "Trainee username");
        return trainerRepository.findNotAssignedToTrainee(traineeUsername);
    }

    private void validateRequired(String val, String field) {
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}