package com.app.fitness.fitnesprogramapp.models.set;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="sets")
public class Set {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "set_seq")
    @SequenceGenerator(name = "set_seq", sequenceName = "set_seq", allocationSize = 50)
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
