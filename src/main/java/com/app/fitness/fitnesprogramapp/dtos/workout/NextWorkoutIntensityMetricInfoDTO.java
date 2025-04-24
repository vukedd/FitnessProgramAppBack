package com.app.fitness.fitnesprogramapp.dtos.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutIntensityMetricInfoDTO { // Renamed
    private Long id;
    private double minimumIntensity;
    private double maximumIntensity;
    private boolean isRange;
    private String title;
    private String metricSymbol;
}