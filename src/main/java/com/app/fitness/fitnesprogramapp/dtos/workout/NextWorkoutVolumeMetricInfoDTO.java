package com.app.fitness.fitnesprogramapp.dtos.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutVolumeMetricInfoDTO { // Renamed
    private Long id;
    private boolean isRange;
    private String title;
    private String metricSymbol;
}