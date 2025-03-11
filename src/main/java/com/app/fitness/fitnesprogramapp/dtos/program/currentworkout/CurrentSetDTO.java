package com.app.fitness.fitnesprogramapp.dtos.program.currentworkout;

import com.app.fitness.fitnesprogramapp.dtos.program.details.IntensityMetricDetailsDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.details.SetIntensityDetailsDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.details.SetVolumeDetailsDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.details.VolumeMetricDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentSetDTO {
    private Long id;

    private SetVolumeDetailsDTO volume;

    private SetIntensityDetailsDTO intensity;

    private VolumeMetricDetailsDTO volumeMetric;

    private IntensityMetricDetailsDTO intensityMetric;

}
