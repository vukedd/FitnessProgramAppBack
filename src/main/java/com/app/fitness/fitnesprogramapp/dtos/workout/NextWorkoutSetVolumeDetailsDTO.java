package com.app.fitness.fitnesprogramapp.dtos.workout;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutSetVolumeDetailsDTO { // Renamed
    private int minimumVolume;
    private int maximumVolume;
}