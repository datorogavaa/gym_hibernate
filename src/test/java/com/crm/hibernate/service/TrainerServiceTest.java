package com.crm.hibernate.service;

import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.entity.User;
import com.crm.hibernate.repository.TrainerRepository;
import com.crm.hibernate.repository.TrainingTypeRepository;
import com.crm.hibernate.security.AuthenticationService;
import com.crm.hibernate.security.PasswordGenerator;
import com.crm.hibernate.security.UsernameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private AuthenticationService authService;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void createProfileCreatesTrainerWithSpecialization() {
        TrainingType specialization = new TrainingType(7L, "Java");
        when(trainingTypeRepository.findById(7L)).thenReturn(Optional.of(specialization));
        when(usernameGenerator.generate("Alice", "Brown")).thenReturn("Alice.Brown");
        when(passwordGenerator.generate()).thenReturn("pass123");

        Trainer created = trainerService.createProfile("Alice", "Brown", 7L);

        assertNotNull(created);
        assertEquals("Alice.Brown", created.getUser().getUsername());
        assertEquals(specialization, created.getSpecialization());
        verify(trainerRepository).save(created);
    }

    @Test
    void selectProfileAuthenticatesAndReturnsTrainer() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User("Sam", "Green", "sam.green", "secret", true));
        when(trainerRepository.findByUsername("sam.green")).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.selectProfile("admin", "adminPass", "sam.green");

        assertSame(trainer, result);
        verify(authService).authenticate("admin", "adminPass");
    }

    @Test
    void updateProfileChangesTrainerFields() {
        // 1. Arrange
        User user = new User("Sam", "Green", "sam.green", "secret", true);
        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(new TrainingType(7L, "Java"));

        doNothing().when(authService).authenticate("sam.green", "adminPass");

        when(trainerRepository.findByUsername("sam.green")).thenReturn(Optional.of(trainer));

        Trainer updated = trainerService.updateProfile(
                "sam.green", "adminPass", "sam.green", "Sam", "Green", false
        );

        assertNotNull(updated);
        assertEquals("Java", updated.getSpecialization().getTrainingTypeName());
        assertFalse(updated.getUser().getIsActive());

        verify(authService).authenticate("sam.green", "adminPass");
        verify(trainerRepository).findByUsername("sam.green");
        verify(trainerRepository).save(trainer);
    }
    @Test
    void getUnassignedTrainersDelegatesToRepository() {
        List<Trainer> trainers = List.of(new Trainer());
        when(trainerRepository.findNotAssignedToTrainee("trainee.user")).thenReturn(trainers);

        List<Trainer> result = trainerService.getUnassignedTrainers("admin", "adminPass", "trainee.user");

        assertEquals(trainers, result);
    }

    @Test
    void activateDeactivateTogglesUserActiveState() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User("Sam", "Green", "sam.green", "secret", true));
        when(trainerRepository.findByUsername("sam.green")).thenReturn(Optional.of(trainer));

        trainerService.activateDeactivate("admin", "adminPass", "sam.green");

        assertEquals(false, trainer.getUser().getIsActive());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void createProfileRejectsMissingSpecialization() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.createProfile("Alice", "Brown", null));
        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> trainerService.createProfile("Alice", "Brown", 99L));
    }

    @Test
    void selectProfileRejectsBlankUsername() {
        assertThrows(IllegalArgumentException.class, () -> trainerService.selectProfile("admin", "pass", "   "));
    }
}