package com.app.fitness.fitnesprogramapp.services.program;

import com.app.fitness.fitnesprogramapp.dtos.program.*;
import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import com.app.fitness.fitnesprogramapp.models.program.*;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.repositories.metrics.IntensityMetricRepository;
import com.app.fitness.fitnesprogramapp.repositories.metrics.VolumeMetricRepository;
import com.app.fitness.fitnesprogramapp.repositories.program.*;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProgramService {
    private final ProgramRepository programRepository;
    private final WeekRepository weekRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final WorkoutExerciseSetRepository workoutExerciseSetRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final VolumeMetricRepository volumeMetricRepository;
    private final IntensityMetricRepository intensityMetricRepository;
    private final ObjectMapper objectMapper;


    public Page<ProgramOverviewDTO> getAllProgramsOverview(Pageable pageable) {
        Page<Program> programPage = programRepository.findAll(pageable);
        return programPage.map(ProgramOverviewDTO::fromEntity);
    }
    public ProgramCreateDTO convertJsonToDTO(String programJson) throws IOException {
        return objectMapper.readValue(programJson, ProgramCreateDTO.class);
    }

    @Transactional
    public Program createProgram(ProgramCreateDTO programDTO, MultipartFile image, String username) throws IOException {
        // Get current user
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new program
        Program program = new Program();
        program.setName(programDTO.getName());
        program.setCreator(currentUser);
        program.setFollowersNumber(0);

        // Process image if provided
        if (image != null && !image.isEmpty()) {
            program.setImageData(image.getBytes());
        }

        // Create weeks, workouts, exercises and sets
        List<Week> weeks = new ArrayList<>();
        for (WeekDTO weekDTO : programDTO.getWeeks()) {
            Week week = createWeek(weekDTO);
            weeks.add(week);
        }

        program.setWeeks(weeks);
        return programRepository.save(program);
    }

    public Optional<Program> getProgramById(Long id) {
        return programRepository.findById(id);
    }

        private Week createWeek(WeekDTO weekDTO) {
        Week week = new Week();
        List<Workout> workouts = new ArrayList<>();

        for (WorkoutDTO workoutDTO : weekDTO.getWorkouts()) {
            Workout workout = createWorkout(workoutDTO);
            workouts.add(workout);
        }

        week.setWorkouts(workouts);
        return weekRepository.save(week);
    }

    private Workout createWorkout(WorkoutDTO workoutDTO) {
        Workout workout = new Workout();
        workout.setTitle(workoutDTO.getTitle());
        workout.setDescription(workoutDTO.getDescription());
        workout.setNumber(workoutDTO.getNumber());

        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        for (WorkoutExerciseDTO exerciseDTO : workoutDTO.getWorkoutExercises()) {
            WorkoutExercise workoutExercise = createWorkoutExercise(exerciseDTO);
            workoutExercises.add(workoutExercise);
        }

        workout.setWorkoutExercises(workoutExercises);
        return workoutRepository.save(workout);
    }

    private WorkoutExercise createWorkoutExercise(WorkoutExerciseDTO exerciseDTO) {
        WorkoutExercise workoutExercise = new WorkoutExercise();

        // Get exercise by ID
        Exercise exercise = exerciseRepository.findById(exerciseDTO.getExercise())
                .orElseThrow(() -> new RuntimeException("Exercise not found with ID: " + exerciseDTO.getExercise()));

        workoutExercise.setExercise(exercise);

        // Create sets
        List<WorkoutExerciseSet> sets = new ArrayList<>();
        for (WorkoutExerciseSetDTO setDTO : exerciseDTO.getWorkoutExerciseSets()) {
            WorkoutExerciseSet set = createWorkoutExerciseSet(
                    setDTO
            );
            sets.add(set);
        }

        workoutExercise.setWorkoutExerciseSets(sets);
        return workoutExerciseRepository.save(workoutExercise);
    }

    private WorkoutExerciseSet createWorkoutExerciseSet(
            WorkoutExerciseSetDTO setDTO) {

        WorkoutExerciseSet set = new WorkoutExerciseSet();

        // Get the metric entities by ID
        VolumeMetric volumeMetric = volumeMetricRepository.findById(setDTO.getVolumeMetric())
                .orElseThrow(() -> new RuntimeException("Volume metric not found with ID: " + setDTO.getVolumeMetric()));

        IntensityMetric intensityMetric = intensityMetricRepository.findById(setDTO.getIntensityMetric())
                .orElseThrow(() -> new RuntimeException("Intensity metric not found with ID: " + setDTO.getIntensityMetric()));

        // Set the metric references
        set.setVolumeMetric(volumeMetric);
        set.setIntensityMetric(intensityMetric);

        // Create and set embedded volume
        SetVolume volume = new SetVolume();
        volume.setMinimumVolume(setDTO.getVolumeMin());
        volume.setMaximumVolume(setDTO.getVolumeMax());
        set.setVolume(volume);

        // Create and set embedded intensity
        SetIntensity intensity = new SetIntensity();
        intensity.setMinimumIntensity(setDTO.getIntensityMin());
        intensity.setMaximumIntensity(setDTO.getIntensityMax());
        set.setIntensity(intensity);

        return workoutExerciseSetRepository.save(set);
    }
}