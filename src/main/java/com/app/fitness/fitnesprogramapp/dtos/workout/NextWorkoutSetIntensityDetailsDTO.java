package com.app.fitness.fitnesprogramapp.dtos.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutSetIntensityDetailsDTO { // Renamed
    private double minimumIntensity;
    private double maximumIntensity;
}