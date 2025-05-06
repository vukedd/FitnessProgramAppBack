package com.app.fitness.fitnesprogramapp.dtos.program.update;

import lombok.Data;

import java.util.List;

@Data
public class UpdateWorkoutDTO {
    private Long id;
    private String title;
    private String description;
    private List<UpdateWorkoutExerciseDTO> workoutExercises;
}
