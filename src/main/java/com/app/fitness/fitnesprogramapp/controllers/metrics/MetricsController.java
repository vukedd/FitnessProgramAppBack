package com.app.fitness.fitnesprogramapp.controllers.metrics;

import com.app.fitness.fitnesprogramapp.dtos.metrics.IntensityMetricDto;
import com.app.fitness.fitnesprogramapp.dtos.metrics.VolumeMetricDto;
import com.app.fitness.fitnesprogramapp.services.metrics.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@CrossOrigin
public class MetricsController {
    private final MetricsService metricsService;

    @GetMapping("/volume-metrics")
    public ResponseEntity<List<VolumeMetricDto>> getAllVolumeMetrics(){
        return ResponseEntity.ok(metricsService.getVolumeMetrics());
    }

    @GetMapping("/intensity-metrics")
    public ResponseEntity<List<IntensityMetricDto>> getAllIntensityMetrics(){
        return ResponseEntity.ok(metricsService.getIntensityMetrics());
    }

}
