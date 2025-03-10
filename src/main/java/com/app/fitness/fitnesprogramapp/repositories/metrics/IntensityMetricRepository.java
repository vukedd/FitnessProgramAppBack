package com.app.fitness.fitnesprogramapp.repositories.metrics;

import com.app.fitness.fitnesprogramapp.models.set.IntensityMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IntensityMetricRepository extends JpaRepository<IntensityMetric, Long> {
}
