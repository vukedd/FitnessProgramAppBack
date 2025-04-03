package com.app.fitness.fitnesprogramapp.dtos.workout;

import com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutDetailsDTO; // Import renamed
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutDTO {
    private NextWorkoutDetailsDTO nextWorkoutDetails; // Renamed field
    private String action;
    private String message;
}