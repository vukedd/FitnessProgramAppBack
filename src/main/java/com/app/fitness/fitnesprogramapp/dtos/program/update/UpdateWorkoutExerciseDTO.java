package com.app.fitness.fitnesprogramapp.dtos.program.update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UpdateWorkoutExerciseDTO {
    private Long id;
    private Long exercise; // Exercise ID
    private int minimumRestTime;
    private int maximumRestTime;
    private List<UpdateWorkoutExerciseSetDTO> sets;
}
