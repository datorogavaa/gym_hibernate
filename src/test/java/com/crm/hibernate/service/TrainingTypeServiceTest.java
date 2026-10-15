package com.crm.hibernate.service;

import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    void getAllTrainingTypesReturnsRepositoryData() {
        List<TrainingType> expected = List.of(new TrainingType(1L, "FITNESS"), new TrainingType(2L, "YOGA"));
        when(trainingTypeRepository.findAll()).thenReturn(expected);

        List<TrainingType> result = trainingTypeService.getAllTrainingTypes();

        assertEquals(expected, result);
        verify(trainingTypeRepository).findAll();
    }
}
