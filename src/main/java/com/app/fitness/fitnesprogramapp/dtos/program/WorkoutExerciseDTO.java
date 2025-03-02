package com.app.fitness.fitnesprogramapp.dtos.program;

import lombok.Data;

import java.util.List;

@Data
public class WorkoutExerciseDTO {
    private Long exercise; // Exercise ID
    private List<WorkoutExerciseSetDTO> workoutExerciseSets;
}
