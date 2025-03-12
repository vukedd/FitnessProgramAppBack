package com.app.fitness.fitnesprogramapp.dtos.program.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetDetailsDTO {
    private Long id;

    private SetVolumeDetailsDTO volume;

    private SetIntensityDetailsDTO intensity;

    private VolumeMetricDetailsDTO volumeMetric;

    private IntensityMetricDetailsDTO intensityMetric;

}
