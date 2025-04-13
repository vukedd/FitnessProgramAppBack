package com.app.fitness.fitnesprogramapp.dtos.program;

import lombok.Data;

@Data
public class CreateSetDTO {
    private int volumeMin;
    private int volumeMax;
    private Long volumeMetric;
    private Long intensityMetric;
    private int intensityMin;
    private int intensityMax;
}
