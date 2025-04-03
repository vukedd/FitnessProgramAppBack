package com.app.fitness.fitnesprogramapp.dtos.workout;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutExerciseMuscleLinkDTO { // Renamed
    private Long id;
    // Skip Exercise back-reference
    private com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutMuscleInfoDTO muscle; // Renamed type
    private Integer intensity;
}