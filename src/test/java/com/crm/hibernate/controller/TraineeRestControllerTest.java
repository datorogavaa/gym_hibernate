package com.crm.hibernate.controller;

import com.crm.hibernate.dto.TraineeProfileResponseDto;
import com.crm.hibernate.dto.TraineeRequestDto;
import com.crm.hibernate.dto.TraineeResponseDto;
import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.entity.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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
        trainee.getUser().setGeneratedPassword("generatedPass");

        Date dob = new Date();
        when(traineeService.createProfile("John", "Doe", dob, "Main Street")).thenReturn(trainee);

        TraineeRequestDto request = new TraineeRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(dob);
        request.setAddress("Main Street");

        ResponseEntity<TraineeResponseDto> response = traineeRestController.createTrainee(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
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

        when(traineeService.selectProfile("john.doe")).thenReturn(trainee);

        ResponseEntity<TraineeProfileResponseDto> response = traineeRestController.getTraineeProfile("john.doe");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals(1, response.getBody().getTrainers().size());
        assertEquals("sam.green", response.getBody().getTrainers().get(0).getUsername());
    }

    @Test
    void updateTraineeProfileReturnsUpdatedDto() {
        Trainee trainee = new Trainee();
        trainee.setUser(new User("John", "Doe", "john.doe", "secret", true));
        trainee.setAddress("New Street");
        trainee.setDateOfBirth(new Date(2000L));

        when(traineeService.updateProfile("john.doe", "John", "Doe", new Date(2000L), "New Street", true))
                .thenReturn(trainee);

        TraineeRequestDto request = new TraineeRequestDto();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(new Date(2000L));
        request.setAddress("New Street");
        request.setIsActive(true);

        ResponseEntity<TraineeProfileResponseDto> response = traineeRestController.updateTraineeProfile("john.doe", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Street", response.getBody().getAddress());
        assertEquals(true, response.getBody().isActive());
    }

    @Test
    void deleteTraineeProfileReturnsOk() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("john.doe", "secret");
        ResponseEntity<Void> response = traineeRestController.deleteTraineeProfile(
                "john.doe", authentication, mock(HttpServletRequest.class), mock(HttpServletResponse.class));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService).deleteProfile("john.doe");
    }

    @Test
    void updateTraineeStatusUsesTraineeService() {
        ResponseEntity<Void> response = traineeRestController.updateTraineeStatus("john.doe", true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(traineeService).activateDeactivate("john.doe", true);
    }
}
