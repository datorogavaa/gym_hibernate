package com.crm.hibernate.restcontrollers;


import com.crm.hibernate.dto.*;
import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Training;
import com.crm.hibernate.service.TraineeService;
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

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainee API", description = "Trainee profile and training operations")
public class TraineeRestController {
    private static final Logger log = LoggerFactory.getLogger(TraineeRestController.class);

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final UserService userService;

    public TraineeRestController(TraineeService traineeService, TrainingService trainingService, UserService userService) {
        this.traineeService = traineeService;
        this.trainingService = trainingService;
        this.userService = userService;
    }


    @Operation(summary = "Register a trainee", description = "Creates a trainee profile and returns generated credentials")
    @ApiResponse(responseCode = "200", description = "Created")
    @PostMapping
    public ResponseEntity<TraineeResponseDto> createTrainee(@Valid @RequestBody TraineeRequestDto trainee) {
        log.info("Creating trainee profile for {} {}", trainee.getFirstName(), trainee.getLastName());
        Trainee createdTrainee = traineeService.createProfile(
                trainee.getFirstName(),
                trainee.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress()
        );
        TraineeResponseDto responseDto = new TraineeResponseDto(
                createdTrainee.getUser().getUsername(),
                createdTrainee.getUser().getPassword()
        );
        return ResponseEntity.ok(responseDto);

    }

    @Operation(summary = "Get trainee profile", description = "Retrieves a trainee profile after authentication")
    @ApiResponse(responseCode = "200", description = "Profile retrieved")
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponseDto> getTraineeProfile(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass) {

        log.debug("Loading trainee profile for {}", username);
        Trainee trainee = traineeService.selectProfile(authUser, authPass, username);

        List<TraineeProfileResponseDto.TrainerDto> trainerDtos = trainee.getTrainers().stream()
                .map(trainer -> new TraineeProfileResponseDto.TrainerDto(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization()
                ))
                .toList();

        TraineeProfileResponseDto response = new TraineeProfileResponseDto(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().getIsActive(),
                trainerDtos
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get unassigned trainers", description = "Retrieves a list of trainers not assigned to the specified trainee")
    @GetMapping("/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryDto>> getUnassignedTrainers(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass) {
        List<com.crm.hibernate.entity.Trainer> trainers =
                traineeService.getUnassignedTrainers(authUser, authPass, username);
        return ResponseEntity.ok(trainers.stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUser().getUsername(), trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(), trainer.getSpecialization().toString()))
                .toList());
    }


    @Operation(summary = "Update trainee profile", description = "Updates trainee personal data and active status")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @PutMapping("/{username}")
    public ResponseEntity<TraineeProfileResponseDto> updateTraineeProfile(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass,
            @Valid @RequestBody TraineeRequestDto traineeRequest) {

        log.info("Updating trainee profile for {}", username);
        Trainee updatedTrainee = traineeService.updateProfile(
                authUser,
                authPass,
                username,
                traineeRequest.getFirstName(),
                traineeRequest.getLastName(),
                traineeRequest.getDateOfBirth(),
                traineeRequest.getAddress(),
                traineeRequest.getIsActive()
        );

        List<TraineeProfileResponseDto.TrainerDto> trainerDtos = updatedTrainee.getTrainers().stream()
                .map(trainer -> new TraineeProfileResponseDto.TrainerDto(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization()
                ))
                .toList();


        TraineeProfileResponseDto response = new TraineeProfileResponseDto(
                updatedTrainee.getUser().getFirstName(),
                updatedTrainee.getUser().getLastName(),
                updatedTrainee.getDateOfBirth(),
                updatedTrainee.getAddress(),
                updatedTrainee.getUser().getIsActive(),
                trainerDtos
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete trainee profile", description = "Hard-deletes trainee profile and related records")
    @ApiResponse(responseCode = "200", description = "Profile deleted")
    @DeleteMapping( "/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass) {
        log.info("Deleting trainee profile for {}", username);
        traineeService.deleteProfile(authUser, authPass, username);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponseDto>> getTraineeTrainings(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass,
            @RequestParam(value = "periodFrom",required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(value = "periodTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(value = "trainerName", required = false) String trainerName,
            @RequestParam(value = "trainingType", required = false) String trainingType) {

        log.debug("Loading trainings for trainee {}", username);
        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria(
                username,
                periodFrom,
                periodTo,
                trainerName,
                trainingType
        );

        List<Training> trainings = trainingService.getTraineeTrainings(authUser, authPass, criteria);

        List<TrainingResponseDto> response = trainings.stream()
                .map(training -> new TrainingResponseDto(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration() != null ? training.getTrainingDuration().intValue() : 0, // <-- converted to int
                        training.getTrainer().getUser().getUsername()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Activate or deactivate trainee", description = "Toggles a trainee active status")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> activateDeactivateTrainee(
            @PathVariable("username") String username,
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass) {

        log.info("Toggling active status for trainee {}", username);
        userService.activateDeactivate(authUser, authPass, username);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update trainee's trainer list", description = "Replaces the assigned trainers list for a trainee")
    @ApiResponse(responseCode = "200", description = "Trainers list updated successfully")
    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerSummaryDto>> updateTraineeTrainersList(
            @PathVariable("username") String username,
            @Valid @RequestBody UpdateTrainerListRequestDto requestDto) {

        log.info("Updating trainer list for trainee {}", username);

        List<TrainerSummaryDto> updatedTrainers = traineeService.updateTrainersList(
                requestDto.getAuthUser(),
                requestDto.getAuthPass(),
                username,
                requestDto.getTrainerUsernames()
        );

        return ResponseEntity.ok(updatedTrainers);
    }

}
