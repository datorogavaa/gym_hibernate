package com.crm.hibernate.controller;

import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.restcontrollers.TrainingTypeRestController;
import com.crm.hibernate.service.TrainingTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeRestControllerTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingTypeRestController trainingTypeRestController;

    @Test
    void listTrainingTypesReturnsAvailableTypes() {
        TrainingType fitness = new TrainingType(1L, "FITNESS");
        when(trainingTypeService.getAllTrainingTypes()).thenReturn(List.of(fitness));

        ResponseEntity<List<TrainingType>> response = trainingTypeRestController.listTrainingTypes();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("FITNESS", response.getBody().get(0).getTrainingTypeName());
        verify(trainingTypeService).getAllTrainingTypes();
    }
}
