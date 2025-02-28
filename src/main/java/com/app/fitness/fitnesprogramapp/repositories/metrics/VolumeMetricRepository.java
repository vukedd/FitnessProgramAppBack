package com.app.fitness.fitnesprogramapp.repositories.metrics;

import com.app.fitness.fitnesprogramapp.models.program.VolumeMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolumeMetricRepository  extends JpaRepository<VolumeMetric, Long> {
}
