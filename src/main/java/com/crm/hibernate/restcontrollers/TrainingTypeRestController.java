package com.crm.hibernate.restcontrollers;

import com.crm.hibernate.entity.TrainingType;
import com.crm.hibernate.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Training Type API", description = "Available training type values")
public class TrainingTypeRestController {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeRestController.class);
    private final TrainingTypeService trainingTypeService;

    public TrainingTypeRestController(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }


    @Operation(summary = "List training types", description = "Returns all available training types")
    @ApiResponse(responseCode = "200", description = "Available training types returned")
    @GetMapping
    public ResponseEntity<List<TrainingType>> listTrainingTypes(
            @RequestParam("authUser") String authUser,
            @RequestParam("authPass") String authPass) {
        List<TrainingType> trainingTypes = trainingTypeService.getAllTrainingTypes(authUser, authPass);
        log.debug("Returning {} training types", trainingTypes.size());
        return ok(trainingTypes);
    }

    public ResponseEntity<List<TrainingType>> listTrainingTypes() {
        List<TrainingType> trainingTypes = trainingTypeService.getAllTrainingTypes();
        return ok(trainingTypes);
    }
}
