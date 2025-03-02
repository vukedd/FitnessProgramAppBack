package com.app.fitness.fitnesprogramapp.services.program;

import com.app.fitness.fitnesprogramapp.dtos.exercise.ExerciseOverviewDto;
import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import com.app.fitness.fitnesprogramapp.repositories.program.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;

    public List<ExerciseOverviewDto> getExerciseOverview() {
        return exerciseRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ExerciseOverviewDto mapToDto(Exercise exercise) {
        ExerciseOverviewDto exerciseOverviewDto = new ExerciseOverviewDto();
        exerciseOverviewDto.setId(exercise.getId());
        exerciseOverviewDto.setTitle(exercise.getTitle());
        return exerciseOverviewDto;
    }
}
