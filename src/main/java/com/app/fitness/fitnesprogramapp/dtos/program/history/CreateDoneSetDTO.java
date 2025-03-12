package com.app.fitness.fitnesprogramapp.dtos.program.history;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateDoneSetDTO {
    private Long startedWorkoutId;
    private Long workoutExerciseId;
    private Long setId;
    private int volume;
    private int intensity;
    private Double weightLifted;
}
