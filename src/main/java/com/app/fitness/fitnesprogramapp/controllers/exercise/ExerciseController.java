package com.app.fitness.fitnesprogramapp.controllers.exercise;

import com.app.fitness.fitnesprogramapp.dtos.exercise.ExerciseOverviewDto;
import com.app.fitness.fitnesprogramapp.services.program.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
