package com.crm.hibernate.service;

import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.TraineeRepository;
import com.crm.hibernate.repository.TrainerRepository;
import com.crm.hibernate.security.AuthenticationService;
import com.crm.hibernate.security.PasswordGenerator;
import com.crm.hibernate.security.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UsernameGenerator usernameGen;
    private final PasswordGenerator passwordGen;
    private final AuthenticationService authService;

    public TraineeService(TraineeRepository traineeRepository,
                          TrainerRepository trainerRepository,
                          UsernameGenerator usernameGen,
                          PasswordGenerator passwordGen,
                          AuthenticationService authService) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.usernameGen = usernameGen;
        this.passwordGen = passwordGen;
        this.authService = authService;
    }

    @Transactional
    public Trainee createProfile(String firstName, String lastName, Date dob, String address) {
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");

        String username = usernameGen.generate(firstName, lastName);
        String password = passwordGen.generate();
        User user = new User(firstName, lastName, username, password, true);

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(dob);
        trainee.setAddress(address);

        traineeRepository.save(trainee);
        log.info("Created Trainee profile: {}", username);
        return trainee;
    }

    @Transactional(readOnly = true)
    public Trainee selectProfile(String authUser, String authPass, String targetUsername) {
        authService.authenticate(authUser, authPass);
        validateRequired(targetUsername, "Username");
        return traineeRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new NoSuchElementException("Trainee not found"));
    }

    @Transactional
    public Trainee updateProfile(String authUser, String authPass, String targetUsername,
                                 String firstName, String lastName, Date dob, String address, boolean isActive) {
        authService.authenticate(authUser, authPass);
        validateRequired(firstName, "First name");
        validateRequired(lastName, "Last name");

        Trainee trainee = selectProfile(authUser, authPass, targetUsername);
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
    public void activateDeactivate(String authUser, String authPass, String targetUsername) {
        authService.authenticate(authUser, authPass);
        Trainee trainee = selectProfile(authUser, authPass, targetUsername);
        boolean state = !trainee.getUser().getIsActive();
        trainee.getUser().setIsActive(state);
        traineeRepository.save(trainee);
        log.info("Toggled Trainee {} isActive to {}", targetUsername, state);
    }

    @Transactional
    public void deleteProfile(String authUser, String authPass, String targetUsername) {
        authService.authenticate(authUser, authPass);
        Trainee trainee = selectProfile(authUser, authPass, targetUsername);

        trainee.getTrainers().clear();
        traineeRepository.save(trainee);

        traineeRepository.delete(trainee);
        log.info("Cascade deleted Trainee and associated records: {}", targetUsername);
    }

    @Transactional
    public void updateTrainersList(String authUser, String authPass, String traineeUsername, Set<String> trainerUsernames) {
        authService.authenticate(authUser, authPass);
        Trainee trainee = selectProfile(authUser, authPass, targetUsernameOrFallback(traineeUsername));
        List<Trainer> trainers = trainerRepository.findByUsernames(trainerUsernames);
        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);
        traineeRepository.save(trainee);
        log.info("Updated trainers for trainee {}", traineeUsername);
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