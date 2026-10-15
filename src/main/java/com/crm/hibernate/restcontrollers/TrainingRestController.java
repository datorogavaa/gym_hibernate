package com.crm.hibernate.restcontrollers;

import com.crm.hibernate.dto.TrainingRequestDto;
import com.crm.hibernate.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Training API", description = "Training creation and listing operations")
public class TrainingRestController {
    private static final Logger log = LoggerFactory.getLogger(TrainingRestController.class);
    private final TrainingService trainingService;

    public TrainingRestController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @Operation(summary = "Add a training", description = "Creates a training assignment between a trainee and trainer")
    @ApiResponse(responseCode = "200", description = "Training added")
    @PostMapping
    public ResponseEntity<Void> addTraining(@RequestBody TrainingRequestDto request) {
        trainingService.addTraining(
                request.getAuthUser(),
                request.getAuthPass(),
                request.getTraineeUsername(),
                request.getTrainerUsername(),
                request.getTrainingName(),
                request.getTrainingDate(),
                request.getTrainingDuration()
        );
        log.info("Training request accepted for trainee {} and trainer {}",
                request.getTraineeUsername(), request.getTrainerUsername());
        return ResponseEntity.ok().build();
    }
}
