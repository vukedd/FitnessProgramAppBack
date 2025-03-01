package com.app.fitness.fitnesprogramapp.models.program;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class WorkoutExerciseSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private SetVolume volume;

    @Embedded
    private  SetIntensity intensity;

    @ManyToOne
    private VolumeMetric volumeMetric;

    @ManyToOne
    private IntensityMetric intensityMetric;


}
