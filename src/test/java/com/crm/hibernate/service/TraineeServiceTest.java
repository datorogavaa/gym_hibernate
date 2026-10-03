package com.crm.hibernate.service;

import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.TraineeRepository;
import com.crm.hibernate.repository.TrainerRepository;
import com.crm.hibernate.security.AuthenticationService;
import com.crm.hibernate.security.PasswordGenerator;
import com.crm.hibernate.security.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private AuthenticationService authService;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void createProfileCreatesAndPersistsNewTrainee() {
        Date dob = new Date();
        when(usernameGenerator.generate("John", "Doe")).thenReturn("John.Doe");
        when(passwordGenerator.generate()).thenReturn("secret123");

        Trainee created = traineeService.createProfile("John", "Doe", dob, "Main Street");

        assertNotNull(created);
        assertEquals("John.Doe", created.getUser().getUsername());
        assertEquals("secret123", created.getUser().getPassword());
        assertEquals("Main Street", created.getAddress());
        verify(traineeRepository).save(created);
    }

    @Test
    void selectProfileAuthenticatesAndReturnsMatchingTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("John", "Doe", "john.doe", "secret", true));
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.selectProfile("admin", "adminPass", "john.doe");

        assertSame(trainee, result);
        verify(authService).authenticate("admin", "adminPass");
    }

    @Test
    void updateProfileChangesFieldsAndSaves() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("John", "Doe", "john.doe", "secret", true));
        trainee.setAddress("Old address");
        Date newDob = new Date();
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        Trainee updated = traineeService.updateProfile("admin", "adminPass", "john.doe",
                "John", "Smith", newDob, "New address", false);

        assertEquals("Smith", updated.getUser().getLastName());
        assertEquals("New address", updated.getAddress());
        assertFalse(updated.getUser().getIsActive());
        verify(traineeRepository).save(updated);
    }

    @Test
    void deleteProfileClearsTrainersThenDeletesTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("Jane", "Lane", "jane.lane", "secret", true));
        Trainer trainer = new Trainer();
        trainer.setUser(new User("Sam", "Smith", "sam.smith", "secret", true));
        Set<Trainer> trainers = new HashSet<>();
        trainers.add(trainer);
        trainee.setTrainers(trainers);
        when(traineeRepository.findByUsername("jane.lane")).thenReturn(Optional.of(trainee));

        traineeService.deleteProfile("admin", "adminPass", "jane.lane");

        assertTrue(trainee.getTrainers().isEmpty());
        verify(traineeRepository).delete(trainee);
    }

    @Test
    void activateDeactivateTogglesUserActiveState() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("John", "Doe", "john.doe", "secret", true));
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainee));

        traineeService.activateDeactivate("admin", "adminPass", "john.doe");

        assertFalse(trainee.getUser().getIsActive());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void updateTrainersListReplacesTrainerSet() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("John", "Doe", "john.doe", "secret", true));
        trainee.setTrainers(new HashSet<>());
        Trainer trainer1 = new Trainer();
        trainer1.setUser(new User("Sam", "Smith", "sam.smith", "secret", true));
        Trainer trainer2 = new Trainer();
        trainer2.setUser(new User("Lee", "Brown", "lee.brown", "secret", true));
        when(traineeRepository.findByUsername("john.doe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernames(Set.of("sam.smith", "lee.brown"))).thenReturn(List.of(trainer1, trainer2));

        traineeService.updateTrainersList("admin", "adminPass", "john.doe", Set.of("sam.smith", "lee.brown"));

        assertEquals(2, trainee.getTrainers().size());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void selectProfileRejectsBlankTargetUsername() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.selectProfile("admin", "adminPass", "   "));
    }
}
