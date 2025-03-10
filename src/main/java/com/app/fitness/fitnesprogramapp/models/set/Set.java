package com.app.fitness.fitnesprogramapp.models.set;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="sets")
public class Set {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private SetVolume volume;

    @Embedded
    private SetIntensity intensity;

    @ManyToOne
    private VolumeMetric volumeMetric;

    @ManyToOne
    private IntensityMetric intensityMetric;


}
