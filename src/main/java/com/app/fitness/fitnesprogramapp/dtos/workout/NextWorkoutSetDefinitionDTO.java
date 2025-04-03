package com.app.fitness.fitnesprogramapp.dtos.workout;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutSetDefinitionDTO { // Renamed
    private Long id;
    private com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutSetVolumeDetailsDTO volume; // Renamed type
    private com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutSetIntensityDetailsDTO intensity; // Renamed type
    private com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutVolumeMetricInfoDTO volumeMetric; // Renamed type
    private com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutIntensityMetricInfoDTO intensityMetric; // Renamed type
}