package com.app.fitness.fitnesprogramapp.dtos.program.update;

import lombok.Data;

@Data
public class UpdateWorkoutExerciseSetDTO {
    private int id;
    private int volumeMin;
    private int volumeMax;
    private Long volumeMetric;
    private Long intensityMetric;
    private int intensityMin;
    private int intensityMax;
}
