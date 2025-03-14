package com.app.fitness.fitnesprogramapp.controllers.exercise;

import com.app.fitness.fitnesprogramapp.dtos.exercise.ExerciseOverviewDto;
import com.app.fitness.fitnesprogramapp.dtos.exercise.history.ExerciseHistoryDTO;
import com.app.fitness.fitnesprogramapp.services.program.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@CrossOrigin
public class ExerciseController {
    private final ExerciseService exerciseService;

    @GetMapping
    public ResponseEntity<List<ExerciseOverviewDto>> getAllExerciseOverview(){
        return ResponseEntity.ok(exerciseService.getExerciseOverview());
    }

    //group the done sets by their workout exercises
    @GetMapping("/history/{exerciseId}")
    public ResponseEntity<ExerciseHistoryDTO> getExerciseHistory(@PathVariable Long exerciseId, Authentication authentication){
        String username = authentication.getName();
        return ResponseEntity.ok(exerciseService.getExerciseHistory(exerciseId,username));
    }

}
