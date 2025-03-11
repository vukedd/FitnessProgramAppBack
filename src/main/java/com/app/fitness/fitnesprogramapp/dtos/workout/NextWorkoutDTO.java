package com.app.fitness.fitnesprogramapp.dtos.workout;

import com.app.fitness.fitnesprogramapp.dtos.program.details.WorkoutDetailsDTO;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutDTO {
    private StartedWorkout startedWorkout;
    private String action; // "start", "continue", or null
    private String message; // For messages like "Program completed"
}
