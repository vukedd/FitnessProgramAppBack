package com.app.fitness.fitnesprogramapp.dtos.program;

import lombok.Data;

@Data
public class WorkoutExerciseSetDTO {
    private Integer volumeMin;
    private Integer volumeMax;
    private Long volumeMetric;
    private Long intensityMetric;
    private Integer intensityMin;
    private Integer intensityMax;
}
