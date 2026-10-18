package com.crm.hibernate.service;

import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.repository.TrainingTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.crm.hibernate.security.AuthenticationService;

import java.util.List;

@Service
public class TrainingTypeService {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeService.class);
    @Autowired
    private TrainingTypeRepository trainingTypeRepository;
    private AuthenticationService authenticationService;

    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository, AuthenticationService authenticationService) {
        this.trainingTypeRepository = trainingTypeRepository;
        this.authenticationService = authenticationService;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public List<TrainingType> getAllTrainingTypes() {
        List<TrainingType> trainingTypes = trainingTypeRepository.findAll();
        log.debug("Loaded {} training types", trainingTypes.size());
        return trainingTypes;
    }

    public List<TrainingType> getAllTrainingTypes(String authUser, String authPass) {
        authenticationService.authenticate(authUser, authPass);
        return getAllTrainingTypes();
    }
}
