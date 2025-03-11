package com.app.fitness.fitnesprogramapp.dtos.program.history;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DoneSetDTO {
    private Long id;

    private String volumeMetric;
    private String intensityMetric;
    private int minimumVolume;
    private int maximumVolume;
    private int minimumIntensity;
    private int maximumIntensity;
}
