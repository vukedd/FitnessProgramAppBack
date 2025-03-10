package com.app.fitness.fitnesprogramapp.services.metrics;

import com.app.fitness.fitnesprogramapp.dtos.metrics.IntensityMetricDto;
import com.app.fitness.fitnesprogramapp.dtos.metrics.VolumeMetricDto;
import com.app.fitness.fitnesprogramapp.models.set.IntensityMetric;
import com.app.fitness.fitnesprogramapp.models.set.VolumeMetric;
import com.app.fitness.fitnesprogramapp.repositories.metrics.IntensityMetricRepository;
import com.app.fitness.fitnesprogramapp.repositories.metrics.VolumeMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final VolumeMetricRepository volumeMetricRepository;
    private final IntensityMetricRepository intensityMetricRepository;

    public List<VolumeMetricDto> getVolumeMetrics() {
        List<VolumeMetric> volumeMetrics = volumeMetricRepository.findAll();
        return volumeMetrics.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private VolumeMetricDto mapToDto(VolumeMetric entity) {
        VolumeMetricDto dto = new VolumeMetricDto();
        dto.setId(entity.getId());
        dto.setRange(entity.isRange());
        dto.setTitle(entity.getTitle());
        dto.setMetricSymbol(entity.getMetricSymbol());
        return dto;
    }

    public List<IntensityMetricDto> getIntensityMetrics() {
        return intensityMetricRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private IntensityMetricDto mapToDto(IntensityMetric entity) {
        IntensityMetricDto dto = new IntensityMetricDto();
        dto.setId(entity.getId());
        dto.setMaximumIntensity(entity.getMaximumIntensity());
        dto.setMinimumIntensity(entity.getMinimumIntensity());
        dto.setRange(entity.isRange());
        dto.setTitle(entity.getTitle());
        dto.setMetricSymbol(entity.getMetricSymbol());
        return dto;
    }

}
