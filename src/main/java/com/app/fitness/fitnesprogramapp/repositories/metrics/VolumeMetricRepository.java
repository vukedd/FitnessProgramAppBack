package com.app.fitness.fitnesprogramapp.repositories.metrics;

import com.app.fitness.fitnesprogramapp.models.set.VolumeMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VolumeMetricRepository  extends JpaRepository<VolumeMetric, Long> {
}
