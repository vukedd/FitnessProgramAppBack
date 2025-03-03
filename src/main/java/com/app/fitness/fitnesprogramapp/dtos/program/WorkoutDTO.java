package com.app.fitness.fitnesprogramapp.dtos.program;

import lombok.Data;

import java.util.List;

@Data
public class WorkoutDTO {
    private String title;
    private String description;
    private String number;
    private List<WorkoutExerciseDTO> workoutExercises;
}
