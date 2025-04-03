package com.app.fitness.fitnesprogramapp.dtos.program;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class WorkoutExerciseDTO {
    private Long exercise; // Exercise ID
    private int minimumRestTime;
    private int maximumRestTime;
    private List<WorkoutExerciseSetDTO> sets;
}
