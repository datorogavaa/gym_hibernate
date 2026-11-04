package com.crm.hibernate.restcontrollers;

import com.crm.hibernate.dto.TrainerTrainingResponseDto;
import com.crm.hibernate.dto.TrainerTrainingCriteria;
import com.crm.hibernate.dto.TrainerProfileResponseDto;
import com.crm.hibernate.dto.TrainerRequestDto;
import com.crm.hibernate.dto.TrainerResponseDto;
import com.crm.hibernate.dto.TrainerUpdateDto;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.Training;
import com.crm.hibernate.service.TrainerService;
import com.crm.hibernate.service.TrainingService;
import com.crm.hibernate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainer API", description = "Trainer profile and assignment operations")
public class TrainerRestController {
    private static final Logger log = LoggerFactory.getLogger(TrainerRestController.class);

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    public TrainerRestController(TrainerService trainerService, TrainingService trainingService) {
        this.trainingService = trainingService;
        this.trainerService = trainerService;
    }

    @Operation(summary = "Register a trainer", description = "Creates a trainer profile and returns generated credentials")
    @ApiResponse(responseCode = "200", description = "Created")
    @PostMapping
    public ResponseEntity<TrainerResponseDto> createTrainer(@Valid @RequestBody TrainerRequestDto trainer) {
        log.info("Creating trainer profile for {} {}", trainer.getFirstName(), trainer.getLastName());
        Trainer createdTrainer = trainerService.createProfile(
                trainer.getFirstName(),
                trainer.getLastName(),
                trainer.getSpecialization().getId()
        );
        TrainerResponseDto responseDto = new TrainerResponseDto(
                createdTrainer.getUser().getUsername(),
                createdTrainer.getUser().getGeneratedPassword()
        );
        return ResponseEntity.ok(responseDto);

    }


    @Operation(summary = "Get trainer profile", description = "Retrieves a trainer profile after authentication")
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINER') and #username == authentication.name)")
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> getTrainerProfile(
            @PathVariable("username") String username) {

        log.debug("Loading trainer profile for {}", username);
        Trainer trainer = trainerService.selectProfile(username);

        List<TrainerProfileResponseDto.TraineeDto> traineeDtos = trainer.getTrainees().stream()
                .map(trainee -> new TrainerProfileResponseDto.TraineeDto(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                ))
                .toList();

        TrainerProfileResponseDto response = new TrainerProfileResponseDto(
                trainer.getUser().getUsername(),
                trainer.getUser().getFirstName(),
                trainer.getUser().getLastName(),
                trainer.getSpecialization(),
                trainer.getUser().getIsActive(),
                traineeDtos
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update trainer profile", description = "Updates trainer personal data and active status")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINER') and #username == authentication.name)")
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> updateTrainer(
            @PathVariable("username") String username,
            @Valid @RequestBody TrainerUpdateDto updateDto) {

        log.info("Updating trainer profile for {}", username);
        Trainer updatedTrainer = trainerService.updateProfile(
                username,
                updateDto.getFirstName(),
                updateDto.getLastName(),
                updateDto.getIsActive()
        );

        List<TrainerProfileResponseDto.TraineeDto> traineeDtos = updatedTrainer.getTrainees().stream()
                .map(trainee -> new TrainerProfileResponseDto.TraineeDto(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                ))
                .toList();

        TrainerProfileResponseDto response = new TrainerProfileResponseDto(
                updatedTrainer.getUser().getUsername(),
                updatedTrainer.getUser().getFirstName(),
                updatedTrainer.getUser().getLastName(),
                updatedTrainer.getSpecialization(),
                updatedTrainer.getUser().getIsActive(),
                traineeDtos
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get trainer trainings", description = "Retrieves a list of trainings for the specified trainer")
    @ApiResponse(responseCode = "200", description = "Trainings retrieved")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINER') and #username == authentication.name)")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponseDto>> getTrainerTrainings(
            @PathVariable("username") String username,
            @RequestParam(value = "periodFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(value = "periodTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(value = "traineeName", required = false) String traineeName) {

        log.debug("Loading trainings for trainer {}", username);
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(
                username,
                periodFrom,
                periodTo,
                traineeName
        );

        List<Training> trainings = trainingService.getTrainerTrainings(criteria);

        List<TrainerTrainingResponseDto> response = trainings.stream()
                .map(training -> new TrainerTrainingResponseDto(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        training.getTrainee().getUser().getUsername()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Activate or deactivate trainer", description = "Sets a trainer's active status explicitly")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TRAINER') and #username == authentication.name)")
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> updateTrainerStatus(
            @PathVariable("username") String username,
            @RequestParam("isActive") boolean isActive) {

        log.info("Updating active status for trainer {} to {}", username, isActive);
        trainerService.activateDeactivate(username, isActive);
        return ResponseEntity.ok().build();
    }

}
