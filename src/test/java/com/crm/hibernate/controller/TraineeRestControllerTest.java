package com.crm.hibernate.controller;

import com.crm.hibernate.dto.*;
import com.crm.hibernate.entity.*;
import com.crm.hibernate.restcontrollers.TraineeRestController;
import com.crm.hibernate.service.TraineeService;
import com.crm.hibernate.service.TrainingService;
import com.crm.hibernate.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeRestControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TraineeRestController traineeRestController;

    @Test
    void createTraineeReturnsGeneratedCredentials() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("John", "Doe", "john.doe", "generatedPass", true));

        Date dob = new Date();
        when(traineeService.createProfile("John", "Doe", dob, "Main Street")).thenReturn(trainee);

        TraineeRequestDto request = new TraineeRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(dob);
        request.setAddress("Main Street");

        ResponseEntity<TraineeResponseDto> response = traineeRestController.createTrainee(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe", response.getBody().getUsername());
        assertEquals("generatedPass", response.getBody().getPassword());
    }

    @Test
    void getTraineeProfileMapsTrainerData() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("John", "Doe", "john.doe", "secret", true));
        trainee.setAddress("Main Street");
        trainee.setDateOfBirth(new Date(0L));

        Trainer trainer = new Trainer();
        trainer.setUser(new User("Sam", "Green", "sam.green", "x", true));
        trainer.setSpecialization(new TrainingType(3L, "CARDIO"));
        trainee.setTrainers(new HashSet<>(Set.of(trainer)));

        when(traineeService.selectProfile("admin", "pass", "john.doe")).thenReturn(trainee);

        ResponseEntity<TraineeProfileResponseDto> response = traineeRestController.getTraineeProfile("john.doe", "admin", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals(1, response.getBody().getTrainers().size());
        assertEquals("sam.green", response.getBody().getTrainers().getFirst().getUsername());
    }

    @Test
    void updateTraineeProfileReturnsUpdatedDto() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("John", "Doe", "john.doe", "secret", true));
        trainee.setAddress("New Street");
        trainee.setDateOfBirth(new Date(2000L));

        when(traineeService.updateProfile("admin", "pass", "john.doe", "John", "Doe", new Date(2000L), "New Street", true))
                .thenReturn(trainee);

        TraineeRequestDto request = new TraineeRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(new Date(2000L));
        request.setAddress("New Street");
        request.setIsActive(true);

        ResponseEntity<TraineeProfileResponseDto> response = traineeRestController.updateTraineeProfile("john.doe", "admin", "pass", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Street", response.getBody().getAddress());
        assertTrue(response.getBody().isActive());
    }

    @Test
    void deleteTraineeProfileReturnsOk() {
        ResponseEntity<Void> response = traineeRestController.deleteTraineeProfile("john.doe", "admin", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService).deleteProfile("admin", "pass", "john.doe");
    }


    @Test
    void getUnassignedTrainersReturnsList() {
        Trainer trainer = new Trainer();
        trainer.setUser(new User("Alex", "Smith", "alex.smith", "pass", true));
        trainer.setSpecialization(new TrainingType(2L, "YOGA"));

        when(traineeService.getUnassignedTrainers("admin", "pass", "john.doe"))
                .thenReturn(List.of(trainer));

        ResponseEntity<List<TrainerSummaryDto>> response =
                traineeRestController.getUnassignedTrainers("john.doe", "admin", "pass");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("alex.smith", response.getBody().getFirst().getUsername());
    }


    @Test
    void getTraineeTrainingsReturnsMappedTrainings() {
        TrainingType type = new TrainingType(1L, "JAVA");

        Trainer trainer = new Trainer();
        trainer.setUser(new User("Trainer", "Bob", "trainer.bob", "pass", true));

        Training training = new Training();
        training.setTrainingName("Java Basics");
        training.setTrainingDate(new Date());
        training.setTrainingType(type);
        training.setTrainingDuration(120.0);
        training.setTrainer(trainer);

        when(trainingService.getTraineeTrainings(eq("admin"), eq("pass"), any(TraineeTrainingCriteria.class)))
                .thenReturn(List.of(training));

        Date periodFrom = new Date();
        Date periodTo = new Date();

        ResponseEntity<List<TrainingResponseDto>> response = traineeRestController.getTraineeTrainings(
                "john.doe", "admin", "pass", periodFrom, periodTo, "Trainer Bob", "JAVA"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Java Basics", response.getBody().get(0).getTrainingName());

        // 120 (int) - ზუსტი ტიპის შემოწმება
        assertEquals(120, response.getBody().get(0).getTrainingDuration());
        assertEquals("trainer.bob", response.getBody().get(0).getTrainerName());
    }

    @Test
    void getTraineeTrainingsHandlesNullDuration() {
        TrainingType type = new TrainingType(1L, "JAVA");

        Trainer trainer = new Trainer();
        trainer.setUser(new User("Trainer", "Bob", "trainer.bob", "pass", true));

        Training training = new Training();
        training.setTrainingName("Java Basics");
        training.setTrainingDate(new Date());
        training.setTrainingType(type);
        training.setTrainingDuration(null); // <-- null duration branch
        training.setTrainer(trainer);

        when(trainingService.getTraineeTrainings(eq("admin"), eq("pass"), any(TraineeTrainingCriteria.class)))
                .thenReturn(List.of(training));

        ResponseEntity<List<TrainingResponseDto>> response = traineeRestController.getTraineeTrainings(
                "john.doe", "admin", "pass", null, null, null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().get(0).getTrainingDuration());
    }

    @Test
    void activateDeactivateTraineeUsesUserService() {
        StatusUpdateRequestDto request = new StatusUpdateRequestDto(true);

        ResponseEntity<Void> response = traineeRestController.activateDeactivateTrainee(
                "john.doe", "admin", "pass", request
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(userService).activateDeactivate("admin", "pass", "john.doe", true);
    }
}