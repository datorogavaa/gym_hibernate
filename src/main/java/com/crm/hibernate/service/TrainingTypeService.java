package com.crm.hibernate.service;

import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.repository.TrainingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class TrainingTypeService {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeService.class);
    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    public List<TrainingType> getAllTrainingTypes() {
        List<TrainingType> trainingTypes = trainingTypeRepository.findAll();
        log.debug("Loaded {} training types", trainingTypes.size());
        return trainingTypes;
    }
}
