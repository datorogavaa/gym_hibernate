package com.crm.hibernate.controller;

import com.crm.hibernate.dto.TrainingRequestDto;
import com.crm.hibernate.restcontrollers.TrainingRestController;
import com.crm.hibernate.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TrainingRestControllerTest {

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingRestController trainingRestController;

    @Test
    void addTrainingReturnsOkAndDelegatesToService() {
        Date trainingDate = new Date();
        TrainingRequestDto request = new TrainingRequestDto(
                "alice.brown",
                "bob.jones",
                "Java Bootcamp",
                trainingDate,
                12.5,
                "admin",
                "adminPass"
        );

        ResponseEntity<Void> response = trainingRestController.addTraining(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(trainingService).addTraining(
                "admin",
                "adminPass",
                "alice.brown",
                "bob.jones",
                "Java Bootcamp",
                trainingDate,
                12.5
        );
    }
}
