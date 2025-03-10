package com.app.fitness.fitnesprogramapp.dtos.program.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutExerciseDetailsDTO {
    private Long id;

    private ExerciseDetailsDTO exercise;

    private List<WorkoutExerciseSetDetailsDTO> workoutExerciseSets;

    private int minimumRestTime;
    private int maximumRestTime;
}
