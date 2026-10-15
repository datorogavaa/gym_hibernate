package com.crm.hibernate.controller;

import com.crm.hibernate.dto.*;
import com.crm.hibernate.entity.*;
import com.crm.hibernate.restcontrollers.TrainerRestController;
import com.crm.hibernate.service.TraineeService;
import com.crm.hibernate.service.TrainerService;
import com.crm.hibernate.service.TrainingService;
import com.crm.hibernate.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerRestControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TrainerRestController trainerRestController;

    @Test
    void createTrainerReturnsGeneratedCredentials() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User("Alice", "Brown", "alice.brown", "secret", true));
        trainer.setSpecialization(new TrainingType(4L, "STRENGTH"));

        TrainerRequestDto request = new TrainerRequestDto();
        request.setFirstName("Alice");
        request.setLastName("Brown");
        request.setSpecialization(new TrainingType(4L, "STRENGTH"));

        when(trainerService.createProfile("Alice", "Brown", 4L)).thenReturn(trainer);

        ResponseEntity<TrainerResponseDto> response = trainerRestController.createTrainer(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("alice.brown", response.getBody().getUsername());
        assertEquals("secret", response.getBody().getPassword());
    }

    @Test
    void getTrainerProfileMapsTrainees() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User("Sam", "Green", "sam.green", "secret", true));
        trainer.setSpecialization(new TrainingType(1L, "FITNESS"));

        Trainee trainee = new Trainee();
        trainee.setUser(new User("Jane", "Doe", "jane.doe", "x", true));
        trainer.setTrainees(Set.of(trainee));

        when(trainerService.selectProfile("admin", "pass", "sam.green")).thenReturn(trainer);

        ResponseEntity<TrainerProfileResponseDto> response = trainerRestController.getTrainerProfile("sam.green", "admin", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("sam.green", response.getBody().getUsername());
        assertEquals(1, response.getBody().getTrainees().size());
        assertEquals("jane.doe", response.getBody().getTrainees().get(0).getUsername());
    }

    @Test
    void updateTrainerProfileReturnsUpdatedData() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User("Sam", "Green", "sam.green", "secret", true));
        trainer.setSpecialization(new TrainingType(2L, "YOGA"));

        TrainerUpdateDto updateDto = new TrainerUpdateDto();
        updateDto.setFirstName("Sam");
        updateDto.setLastName("Green");
        updateDto.setIsActive(true);

        when(trainerService.updateProfile("admin", "pass", "sam.green", "Sam", "Green", true)).thenReturn(trainer);

        ResponseEntity<TrainerProfileResponseDto> response = trainerRestController.updateTrainer("sam.green", "admin", "pass", updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isActive());
    }

    @Test
    void getUnassignedTrainersReturnsList() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User("Sam", "Green", "sam.green", "secret", true));
        trainer.setSpecialization(new TrainingType(3L, "CARDIO"));
        when(trainerService.getUnassignedTrainers("admin", "pass", "jane.doe")).thenReturn(List.of(trainer));

        ResponseEntity<List<TrainerSummaryDto>> response = trainerRestController.getUnassignedTrainers("jane.doe", "admin", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("sam.green", response.getBody().get(0).getUsername());
    }

    @Test
    void getTrainerTrainingsReturnsList() {
        Training training = new Training();
        training.setTrainingName("Core" );
        training.setTrainingDate(new Date());
        training.setTrainingType(new TrainingType(1L, "FITNESS"));
        training.setTrainingDuration(60.0);
        Trainee trainee = new Trainee();
        trainee.setUser(new User("Jane", "Doe", "jane.doe", "secret", true));
        training.setTrainee(trainee);

        when(trainingService.getTrainerTrainings(
                org.mockito.ArgumentMatchers.eq("admin"),
                org.mockito.ArgumentMatchers.eq("pass"),
                org.mockito.ArgumentMatchers.any(TrainerTrainingCriteria.class)))
                .thenReturn(List.of(training));

        ResponseEntity<List<TrainerTrainingResponseDto>> response = trainerRestController.getTrainerTrainings("sam.green", "admin", "pass", null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void activateDeactivateTrainerUsesUserService() {
        ResponseEntity<Void> response = trainerRestController.activateDeactivateTrainer("sam.green", "admin", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService).activateDeactivate("admin", "pass", "sam.green");
    }
}
