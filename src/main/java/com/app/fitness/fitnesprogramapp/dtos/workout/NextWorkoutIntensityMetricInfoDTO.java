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
    private int minimumIntensity;
    private int maximumIntensity;
    private boolean isRange;
    private String title;
    private String metricSymbol;
}