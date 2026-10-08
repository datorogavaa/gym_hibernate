package com.crm.hibernate.restcontrollers;

import com.crm.hibernate.dto.*;
import com.crm.hibernate.entity.Trainee;
import com.crm.hibernate.entity.Trainer;
import com.crm.hibernate.entity.Training;
import com.crm.hibernate.service.TraineeService;
import com.crm.hibernate.service.TrainerService;
import com.crm.hibernate.service.TrainingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
public class TrainerRestController {

    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingService trainingService;
    public TrainerRestController(TrainerService trainerService, TraineeService traineeService, TrainingService trainingService) {
        this.trainingService = trainingService;
        this.trainerService = trainerService;
        this.traineeService = traineeService;
    }

    @PostMapping("/create")
    public ResponseEntity<TrainerResponseDto> createTrainer(@RequestBody TrainerRequestDto trainer) {
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


    // 1. Get Trainer Profile (GET)
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> getTrainerProfile(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass) {

        // Authenticate and fetch trainer
        Trainer trainer = trainerService.selectProfile(authUser, authPass, username);

        // Map assigned trainees to DTO list
        List<TrainerProfileResponseDto.TraineeDto> traineeDtos = trainer.getTrainees().stream()
                .map(trainee -> new TrainerProfileResponseDto.TraineeDto(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                ))
                .toList();

        // Build response
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

    // 2. Update Trainer Profile (PUT)
    @PutMapping("/{username}")
    public ResponseEntity<TrainerProfileResponseDto> updateTrainer(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass,
            @RequestBody TrainerUpdateDto updateDto) {

        // 1. Call your service with all required parameters
        Trainer updatedTrainer = trainerService.updateProfile(
                authUser,
                authPass,
                username,
                updateDto.getFirstName(),
                updateDto.getLastName(),
                updateDto.getIsActive()
        );

        // 2. Map assigned trainees to DTO list
        List<TrainerProfileResponseDto.TraineeDto> traineeDtos = updatedTrainer.getTrainees().stream()
                .map(trainee -> new TrainerProfileResponseDto.TraineeDto(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                ))
                .toList();

        // 3. Build response DTO
        TrainerProfileResponseDto response = new TrainerProfileResponseDto(
                updatedTrainer.getUser().getUsername(),
                updatedTrainer.getUser().getFirstName(),
                updatedTrainer.getUser().getLastName(),
                updatedTrainer.getSpecialization(),
                updatedTrainer.getUser().getIsActive(),
                traineeDtos
        );

        // 4. Return 200 OK
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerSummaryDto>> getUnassignedTrainers(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass) {

        // Assuming you have or will add this method in your TraineeService / TrainerService
        List<Trainer> unassignedTrainers = trainerService.getUnassignedTrainers(authUser, authPass, username);

        List<TrainerSummaryDto> response = unassignedTrainers.stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }


    @PutMapping("/{username}/trainers")
    public ResponseEntity<List<TrainerSummaryDto>> updateTrainersList(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass,
            @RequestBody UpdateTrainerListRequestDto requestDto) {

        // 1. Call your existing service method
        traineeService.updateTrainersList(authUser, authPass, username, requestDto.getTrainerUsernames());

        // 2. Fetch the updated trainee profile to get their new trainer list
        Trainee trainee = traineeService.selectProfile(authUser, authPass, username);

        // 3. Map to response DTO list
        List<TrainerSummaryDto> response = trainee.getTrainers().stream()
                .map(trainer -> new TrainerSummaryDto(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponseDto>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date periodTo,
            @RequestParam(required = false) String traineeName) {

        // 1. Create your criteria object using your exact all-args constructor
        TrainerTrainingCriteria criteria = new TrainerTrainingCriteria(
                username,
                periodFrom,
                periodTo,
                traineeName
        );

        // 2. Call your service method (handles authentication and criteria filtering)
        List<Training> trainings = trainingService.getTrainerTrainings(authUser, authPass, criteria);

        // 3. Map the Training entities to your Response DTO list
        List<TrainerTrainingResponseDto> response = trainings.stream()
                .map(training -> new TrainerTrainingResponseDto(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName(), // Adjust if method name differs
                        training.getTrainingDuration(), // Passes straight in as a Number
                        training.getTrainee().getUser().getUsername()     // Adjust if mapping requires full name
                ))
                .toList();

        // 4. Return 200 OK with the filtered list
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{username}/status")
    public ResponseEntity<Void> activateDeactivateTrainer(
            @PathVariable String username,
            @RequestParam String authUser,
            @RequestParam String authPass) {

        // Calls your trainer service method which toggles the state
        trainerService.activateDeactivate(authUser, authPass, username);

        // Return 200 OK with no content body
        return ResponseEntity.ok().build();
    }

}
