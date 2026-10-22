package com.crm.hibernate.restcontrollers;

import com.crm.hibernate.dto.*;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.Training;
import com.crm.hibernate.service.TrainerService;
import com.crm.hibernate.service.TrainingService;
import com.crm.hibernate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainer API", description = "Trainer profile and assignment operations")
public class TrainerRestController {
    private static final Logger log = LoggerFactory.getLogger(TrainerRestController.class);

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final UserService userService;
    public TrainerRestController(TrainerService trainerService, TrainingService trainingService, UserService userService) {
        this.trainingService = trainingService;
        this.trainerService = trainerService;
        this.userService = userService;
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
                createdTrainer.getUser().getPassword()
        );
        return ResponseEntity.ok(responseDto);

    }


    @Operation(summary = "Get trainer profile", description = "Retrieves a trainer profile after authentication")
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> getTrainerProfile(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass) {

        log.debug("Loading trainer profile for {}", username);
        Trainer trainer = trainerService.selectProfile(authUser, authPass, username);

        List<TrainerProfileResponseDto.TraineeDto> traineeDtos =
                (trainer.getTrainees() == null) ? List.of() :
                        trainer.getTrainees().stream()
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
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> updateTrainer(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass,
            @Valid @RequestBody TrainerUpdateDto updateDto) {

        log.info("Updating trainer profile for {}", username);
        Trainer updatedTrainer = trainerService.updateProfile(
                authUser,
                authPass,
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


    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponseDto>> getTrainerTrainings(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass,
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

        List<Training> trainings = trainingService.getTrainerTrainings(authUser, authPass, criteria);

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


    @PatchMapping("/{username}/status")
    @Operation(summary = "Activate or deactivate trainer", description = "Sets explicit active status for a trainer profile")
    @ApiResponse(responseCode = "200", description = "Status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or trainer already has the requested status")
    public ResponseEntity<Void> activateDeactivateTrainer(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass,
            @Valid @RequestBody StatusUpdateRequestDto request) {

        log.info("Setting active status for trainer {} to {}", username, request.getIsActive());
        userService.activateDeactivate(authUser, authPass, username, request.getIsActive());
        return ResponseEntity.ok().build();
    }


}
