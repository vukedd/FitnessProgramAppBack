package com.app.fitness.fitnesprogramapp.dtos.program.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutDetailsDTO {
    private Long id;

    private String title;
    private String description;
    private String number;

    private List<WorkoutExerciseDetailsDTO> workoutExercises;
}
