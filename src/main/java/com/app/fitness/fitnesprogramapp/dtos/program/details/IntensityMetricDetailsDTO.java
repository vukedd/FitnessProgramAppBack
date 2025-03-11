package com.app.fitness.fitnesprogramapp.dtos.program.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntensityMetricDetailsDTO {
    private Long id;

    private int minimumIntensity;
    private int maximumIntensity;
    private boolean isRange;
    private String title;
    private String metricSymbol;
}
