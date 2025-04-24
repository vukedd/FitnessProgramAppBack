package com.app.fitness.fitnesprogramapp.dtos.metrics;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class IntensityMetricDto {
    private Long id;

    private double minimumIntensity;
    private double maximumIntensity;
    private boolean isRange;
    private String title;
    private String metricSymbol;
}
