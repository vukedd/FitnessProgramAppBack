package com.app.fitness.fitnesprogramapp.dtos.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VolumeMetricDto {
    private Long id;

    private boolean isRange;
    private String title;
    private String metricSymbol;
}
