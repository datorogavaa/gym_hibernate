package com.crm.hibernate.restcontrollers;


import com.crm.hibernate.dto.*;
import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Training;
import com.crm.hibernate.service.TraineeService;
import com.crm.hibernate.service.TrainingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
public class TraineeRestController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;

    public TraineeRestController(TraineeService traineeService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainingService = trainingService;
    }


    @PostMapping("/create")
    public ResponseEntity<TraineeResponseDto> createTrainee(@RequestBody TraineeRequestDto trainee) {
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

    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponseDto> getTraineeProfile(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass) {

        // 1. Calls your service which authenticates and fetches the profile
        Trainee trainee = traineeService.selectProfile(authUser, authPass, username);

        // 2. Map the trainers list to DTOs
        List<TraineeProfileResponseDto.TrainerDto> trainerDtos = trainee.getTrainers().stream()
                .map(trainer -> new TraineeProfileResponseDto.TrainerDto(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization()
                ))
                .toList();

        // 3. Map the main Trainee entity to the Response DTO
        TraineeProfileResponseDto response = new TraineeProfileResponseDto(
                trainee.getUser().getFirstName(),
                trainee.getUser().getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                trainee.getUser().getIsActive(),
                trainerDtos
        );

        // 4. Return 200 OK with the response body
        return ResponseEntity.ok(response);
    }



    @PutMapping("/{username}/update")
    public ResponseEntity<TraineeProfileResponseDto> updateTraineeProfile(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass,
            @RequestBody TraineeRequestDto traineeRequest) {

        // 1. Calls your service which authenticates and updates the profile
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

        // 2. Map the trainers list to DTOs
        List<TraineeProfileResponseDto.TrainerDto> trainerDtos = updatedTrainee.getTrainers().stream()
                .map(trainer -> new TraineeProfileResponseDto.TrainerDto(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization()
                ))
                .toList();

        // 3. Map the main Trainee entity to the Response DTO
        TraineeProfileResponseDto response = new TraineeProfileResponseDto(
                updatedTrainee.getUser().getFirstName(),
                updatedTrainee.getUser().getLastName(),
                updatedTrainee.getDateOfBirth(),
                updatedTrainee.getAddress(),
                updatedTrainee.getUser().getIsActive(),
                trainerDtos
        );

        // 4. Return 200 OK with the response body
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{username}/delete")
    public ResponseEntity<Void> deleteTraineeProfile(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass) {
        // 1. Calls your service which authenticates and deletes the profile
        traineeService.deleteProfile(authUser, authPass, username);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainingResponseDto>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) String trainingType) {

        // 1. Create your criteria object using its exact all-args constructor
        TraineeTrainingCriteria criteria = new TraineeTrainingCriteria(
                username,
                periodFrom,
                periodTo,
                trainerName,
                trainingType
        );

        // 2. Call your service method (which checks authentication and fetches trainings)
        List<Training> trainings = trainingService.getTraineeTrainings(authUser, authPass, criteria);

        // 3. Map the Training entities to your Response DTO list
        // 3. Map the Training entities to your Response DTO list
        List<TrainingResponseDto> response = trainings.stream()
                .map(training -> new TrainingResponseDto(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration() != null ? training.getTrainingDuration().intValue() : 0, // <-- converted to int
                        training.getTrainer().getUser().getUsername()
                ))
                .toList();

        // 4. Return 200 OK
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> activateDeactivateTrainee(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass) {

        // Calls your service method which handles authentication and toggles the state
        traineeService.activateDeactivate(authUser, authPass, username);

        // Return 200 OK with no content body
        return ResponseEntity.ok().build();
    }

}
