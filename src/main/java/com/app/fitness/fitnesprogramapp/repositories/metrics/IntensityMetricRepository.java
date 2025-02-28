package com.app.fitness.fitnesprogramapp.repositories.metrics;

import com.app.fitness.fitnesprogramapp.models.program.IntensityMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntensityMetricRepository extends JpaRepository<IntensityMetric, Long> {
}
