package com.crm.hibernate.restcontrollers;

import com.crm.hibernate.dto.TrainingRequestDto;
import com.crm.hibernate.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

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
    @PreAuthorize("hasRole('ADMIN') or " +
            "(hasRole('TRAINEE') and #request.traineeUsername == authentication.name) or " +
            "(hasRole('TRAINER') and #request.trainerUsername == authentication.name)")
    @PostMapping
    public ResponseEntity<Void> addTraining(@RequestBody TrainingRequestDto request) {
        trainingService.addTraining(
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

    @Operation(summary = "Get trainer workload", description = "Retrieves full workload summary for a trainer")
    @ApiResponse(responseCode = "200", description = "Workload retrieved")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINEE') or (hasRole('TRAINER') and #username == authentication.name)")
    @GetMapping("/workload/{username}")
    public ResponseEntity<?> getTrainerWorkload(@PathVariable("username") String username) {
        log.info("Workload summary requested for trainer {}", username);
        return ResponseEntity.ok(trainingService.getTrainerWorkload(username));
    }

    @Operation(summary = "Get trainer monthly workload", description = "Retrieves monthly working hours for a trainer")
    @ApiResponse(responseCode = "200", description = "Monthly workload retrieved")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRAINEE') or (hasRole('TRAINER') and #username == authentication.name)")
    @GetMapping("/workload/{username}/months")
    public ResponseEntity<?> getTrainerMonthlyHours(
            @PathVariable("username") String username,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        log.info("Monthly workload requested for trainer {} ({}-{})", username, year, month);
        return ResponseEntity.ok(trainingService.getTrainerMonthlyHours(username, year, month));
    }
}
