package com.app.fitness.fitnesprogramapp.models.set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class IntensityMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double minimumIntensity;
    private double maximumIntensity;
    private boolean isRange;
    private String title;
    private String metricSymbol;
}
