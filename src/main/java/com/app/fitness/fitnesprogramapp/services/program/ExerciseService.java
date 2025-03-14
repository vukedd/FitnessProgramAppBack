package com.app.fitness.fitnesprogramapp.services.program;

import com.app.fitness.fitnesprogramapp.dtos.exercise.ExerciseOverviewDto;
import com.app.fitness.fitnesprogramapp.dtos.exercise.history.ExerciseHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.exercise.history.WorkoutExerciseExerciseHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.DoneSetsHistoryDTO;
import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import com.app.fitness.fitnesprogramapp.models.program.StartedProgram;
import com.app.fitness.fitnesprogramapp.models.set.DoneSet;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.models.week.StartedWeek;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import com.app.fitness.fitnesprogramapp.repositories.exercise.ExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final ProgramService programService;
    private final ProgramHistoryService programHistoryService;

    public List<ExerciseOverviewDto> getExerciseOverview() {
        return exerciseRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    private ExerciseOverviewDto mapToDto(Exercise exercise) {
        ExerciseOverviewDto exerciseOverviewDto = new ExerciseOverviewDto();
        exerciseOverviewDto.setId(exercise.getId());
        exerciseOverviewDto.setTitle(exercise.getTitle());
        return exerciseOverviewDto;
    }

    public ExerciseHistoryDTO getExerciseHistory(Long exerciseId, String username) {
        Exercise exercise = exerciseRepository.findById(exerciseId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();

        ExerciseHistoryDTO exerciseHistoryDTO = new ExerciseHistoryDTO();
        exerciseHistoryDTO.setExerciseId(exercise.getId());
        exerciseHistoryDTO.setExerciseTitle(exercise.getTitle());
        exerciseHistoryDTO.setWorkoutExercises(new ArrayList<>());

        Map<Long, WorkoutExerciseExerciseHistoryDTO> workoutExerciseMap = new HashMap<>();

        user.getStartedPrograms().stream()
                .flatMap(program -> program.getStartedWeeks().stream())
                .flatMap(week -> week.getStartedWorkouts().stream())
                .flatMap(workout -> workout.getDoneSets().stream())
                .filter(doneSet -> doneSet.getWorkoutExercise().getExercise().getId().equals(exerciseId))
                .forEach(doneSet -> {
                    Long workoutExerciseId = doneSet.getWorkoutExercise().getId();

                    workoutExerciseMap.computeIfAbsent(workoutExerciseId, id -> {
                        WorkoutExerciseExerciseHistoryDTO dto = new WorkoutExerciseExerciseHistoryDTO();
                        dto.setId(id);
                        dto.setDoneSets(new ArrayList<>());
                        exerciseHistoryDTO.getWorkoutExercises().add(dto);
                        return dto;
                    }).getDoneSets().add(programHistoryService.mapDoneSetToHistoryDTO(doneSet));
                });

        return exerciseHistoryDTO;
    }
}
