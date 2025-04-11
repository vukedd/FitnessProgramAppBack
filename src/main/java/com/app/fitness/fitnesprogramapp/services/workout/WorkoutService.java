package com.app.fitness.fitnesprogramapp.services.workout;

import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateDoneSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.set.CompleteSetResponseDTO;
import com.app.fitness.fitnesprogramapp.dtos.workout.*;
import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import com.app.fitness.fitnesprogramapp.models.exercise.ExerciseMuscle;
import com.app.fitness.fitnesprogramapp.models.exercise.Muscle;
import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.program.Program;
import com.app.fitness.fitnesprogramapp.models.program.StartedProgram;
import com.app.fitness.fitnesprogramapp.models.set.*;
import com.app.fitness.fitnesprogramapp.models.set.Set;
import com.app.fitness.fitnesprogramapp.models.week.StartedWeek;
import com.app.fitness.fitnesprogramapp.models.week.Week;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import com.app.fitness.fitnesprogramapp.repositories.exercise.WorkoutExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.program.StartedProgramRepository;
import com.app.fitness.fitnesprogramapp.repositories.set.DoneSetRepository;
import com.app.fitness.fitnesprogramapp.repositories.set.SetRepository;
import com.app.fitness.fitnesprogramapp.repositories.week.StartedWeekRepository;
import com.app.fitness.fitnesprogramapp.repositories.workout.StartedWorkoutRepository;
import com.app.fitness.fitnesprogramapp.repositories.workout.WorkoutRepository;
import com.app.fitness.fitnesprogramapp.services.program.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    private final StartedWeekRepository startedWeekRepository;
    private final StartedProgramRepository startedProgramRepository;
    private final StartedWorkoutRepository startedWorkoutRepository;
    private final DoneSetRepository doneSetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final SetRepository setRepository;
    private final WorkoutRepository workoutRepository;
    private final ProgramService programService; // For workout exercise mapping

    /**
     * Processes the next workout request for a given program ID
     * @param programId The started program ID
     * @return NextWorkoutDTO with details, or null if program not found
     */
    public NextWorkoutDTO processNextWorkout(Long programId) {
        Optional<StartedProgram> startedProgramOpt = startedProgramRepository.findById(programId);

        if (startedProgramOpt.isEmpty()) {
            return null;
        }

        StartedProgram startedProgram = startedProgramOpt.get();
        NextWorkoutDTO nextWorkoutDTO = getNextWorkout(startedProgram);

        // If we need to start a new workout, create it and save
        if (nextWorkoutDTO.getNextWorkoutDetails() != null && "start".equals(nextWorkoutDTO.getAction())) {
            Workout workoutToStart = workoutRepository.findById(nextWorkoutDTO.getNextWorkoutDetails().getWorkoutId())
                    .orElseThrow(() -> new RuntimeException("Workout not found"));

            // Create a deep copy of the workout
            StartedWorkout newStartedWorkout = createStartedWorkoutFromWorkout(workoutToStart);
            newStartedWorkout.setStartDate(new Date());
            newStartedWorkout.setFinished(false);

            // Save the new started workout
            StartedWorkout savedWorkout = startedWorkoutRepository.save(newStartedWorkout);

            // Add it to the latest started week
            StartedWeek latestStartedWeek = startedProgram.getStartedWeeks().getLast();
            latestStartedWeek.getStartedWorkouts().add(savedWorkout);

            // Save the started week
            startedWeekRepository.save(latestStartedWeek);

            // Update the DTO with the persisted started workout
            nextWorkoutDTO.setNextWorkoutDetails(mapStartedWorkoutToNextWorkoutDetailsDTO(savedWorkout));
        }

        return nextWorkoutDTO;
    }

    /**
     * Creates a deep copy of Workout into StartedWorkout
     * @param workout The original workout
     * @return A new StartedWorkout with copied properties
     */
    private StartedWorkout createStartedWorkoutFromWorkout(Workout workout) {
        StartedWorkout startedWorkout = new StartedWorkout();

        // Copy basic properties
        startedWorkout.setTitle(workout.getTitle());
        startedWorkout.setDescription(workout.getDescription());
        startedWorkout.setNumber(workout.getNumber());
        startedWorkout.setWorkoutId(workout.getId()); // Store the original workout ID

        startedWorkout.setWorkoutExercises(new ArrayList<>());
        for(WorkoutExercise workoutExercise: workout.getWorkoutExercises()) {
            WorkoutExercise exercise = new WorkoutExercise();
            exercise.setExercise(workoutExercise.getExercise());
            exercise.setMaximumRestTime(workoutExercise.getMaximumRestTime());
            exercise.setMinimumRestTime(workoutExercise.getMinimumRestTime());
            exercise.setSets(new ArrayList<>());
            for(Set set: workoutExercise.getSets()) {
                Set s = new Set();
                SetIntensity intensity = new SetIntensity();
                SetVolume volume = new SetVolume();
                intensity.setMaximumIntensity(set.getIntensity().getMaximumIntensity());
                intensity.setMinimumIntensity(set.getIntensity().getMinimumIntensity());
                volume.setMaximumVolume(set.getVolume().getMaximumVolume());
                volume.setMinimumVolume(set.getVolume().getMinimumVolume());
                s.setIntensity(intensity);
                s.setVolume(volume);
                s.setIntensityMetric(set.getIntensityMetric());
                s.setVolumeMetric(set.getVolumeMetric());
                setRepository.save(s);
                exercise.getSets().add(s);
            }
            workoutExerciseRepository.save(exercise);
            startedWorkout.getWorkoutExercises().add(exercise);

        }

        startedWorkout.setDoneSets(new ArrayList<>());

        return startedWorkout;
    }

    /**
     * Gets the next workout for a user based on their started program
     * @param startedProgram The program the user has started
     * @return NextWorkoutDTO containing the next workout and action details
     */
    public NextWorkoutDTO getNextWorkout(StartedProgram startedProgram) {
        // Check if program is finished
        if (startedProgram.isFinished()) {
            return NextWorkoutDTO.builder()
                    .message("Program is already completed")
                    .build();
        }

        // Get the current started weeks
        List<StartedWeek> startedWeeks = startedProgram.getStartedWeeks();

        if (startedWeeks.isEmpty()) {
            // No weeks started yet, start the first week of the program
            return startFirstWeek(startedProgram);
        }

        // Sort weeks by ID to ensure we get the latest one
        startedWeeks.sort(Comparator.comparing(StartedWeek::getId));
        StartedWeek latestStartedWeek = startedWeeks.getLast();

        if (!latestStartedWeek.isFinished()) {
            // Week is not finished, look for the next workout to do in this week
            return getNextWorkoutInWeek(latestStartedWeek, startedProgram.getProgram(),startedProgram);
        } else {
            // Current week is finished, start next week
            return startNextWeek(startedProgram);
        }
    }

    /**
     * Gets the next workout to do in the current week
     * @param startedWeek The current week
     * @param program The program containing the week
     * @return NextWorkoutDTO with the next workout and action
     */
    private NextWorkoutDTO getNextWorkoutInWeek(StartedWeek startedWeek, Program program,StartedProgram startedProgram) {
        // Get the original week from program using weekId

        Optional<Week> optionalWeek = program.getWeeks().stream()
                .filter(w -> w.getId().equals(startedWeek.getWeekId()))
                .findFirst();

        if(optionalWeek.isEmpty()){
            startedWeek.setFinished(true);
            startedWeek.setDoneDate(new Date());
            startedWeekRepository.save(startedWeek);
            return startNextWeek(startedProgram);
        }
        Week originalWeek=optionalWeek.get();

        List<StartedWorkout> startedWorkouts = startedWeek.getStartedWorkouts();

        if (startedWorkouts.isEmpty()) {
            // No workouts started in this week, start the first workout
            if (originalWeek.getWorkouts().isEmpty()) {
                return NextWorkoutDTO.builder()
                        .message("No workouts defined for this week")
                        .build();
            }

            // Start the first workout of the week
            Workout nextWorkout = originalWeek.getWorkouts().getFirst();

            return NextWorkoutDTO.builder()
                    .nextWorkoutDetails(createNextWorkoutDetailsDTO(nextWorkout))
                    .action("start")
                    .build();
        }

        // Check if there's an unfinished workout
        Optional<StartedWorkout> unfinishedWorkout = startedWorkouts.stream()
                .filter(w -> !w.isFinished())
                .findFirst();

        if (unfinishedWorkout.isPresent()) {
            // Continue with the unfinished workout
            return NextWorkoutDTO.builder()
                    .nextWorkoutDetails(mapStartedWorkoutToNextWorkoutDetailsDTO(unfinishedWorkout.get()))
                    .action("continue")
                    .build();
        }

        // All started workouts are finished, find the next workout in the week
        // Collect IDs of already started workouts
        java.util.Set<Long> startedWorkoutIds = startedWorkouts.stream()
                .map(StartedWorkout::getWorkoutId)
                .collect(Collectors.toSet());

        // Find the first workout in the original week that hasn't been started yet
        Optional<Workout> nextWorkout = originalWeek.getWorkouts().stream()
                .filter(workout -> !startedWorkoutIds.contains(workout.getId()))
                .findFirst();

        if (nextWorkout.isPresent()) {
            // Start the next workout
            return NextWorkoutDTO.builder()
                    .nextWorkoutDetails(createNextWorkoutDetailsDTO(nextWorkout.get()))
                    .action("start")
                    .build();
        } else {
            // All workouts in this week have been started and finished
            // Mark the week as finished
            startedWeek.setFinished(true);
            startedWeek.setDoneDate(new Date());
            startedWeekRepository.save(startedWeek);

            return NextWorkoutDTO.builder()
                    .message("Week completed")
                    .build();
        }
    }

    /**
     * Creates NextWorkoutDetailsDTO from a Workout
     * Used for workouts that haven't been started yet
     */
    private NextWorkoutDetailsDTO createNextWorkoutDetailsDTO(Workout workout) {
        // Create a new NextWorkoutDetailsDTO using the builder pattern
        return NextWorkoutDetailsDTO.builder()
                .workoutId(workout.getId())
                .title(workout.getTitle())
                .description(workout.getDescription())
                .number(workout.getNumber())
                .workoutExercises(programService.mapWorkoutExercisesToDetailsDTOs(workout.getWorkoutExercises()))
                .doneSets(new ArrayList<>())
                .finished(false)
                .build();
    }

    /**
     * Starts the first week of a program
     * @param startedProgram The program to start
     * @return NextWorkoutDTO with the first workout
     */
    private NextWorkoutDTO startFirstWeek(StartedProgram startedProgram) {
        Program program = startedProgram.getProgram();
        List<Week> programWeeks = program.getWeeks();

        if (programWeeks.isEmpty()) {
            return NextWorkoutDTO.builder()
                    .message("No weeks defined for this program")
                    .build();
        }

        // Get the first week
        Week firstWeek = programWeeks.getFirst();

        // Create a new started week (as a copy of the first week)
        StartedWeek newStartedWeek = new StartedWeek();
        newStartedWeek.setWeekId(firstWeek.getId());
        newStartedWeek.setStartDate(new Date());
        newStartedWeek.setFinished(false);
        newStartedWeek.setStartedWorkouts(new ArrayList<>());
        startedWeekRepository.save(newStartedWeek);

        // Add to the started program
        startedProgram.getStartedWeeks().add(newStartedWeek);
        startedProgramRepository.save(startedProgram);

        // Get the first workout of the first week
        if (firstWeek.getWorkouts().isEmpty()) {
            return NextWorkoutDTO.builder()
                    .message("No workouts defined for the first week")
                    .build();
        }

        Workout firstWorkout = firstWeek.getWorkouts().getFirst();

        return NextWorkoutDTO.builder()
                .nextWorkoutDetails(createNextWorkoutDetailsDTO(firstWorkout))
                .action("start")
                .build();
    }

    /**
     * Starts the next week after a completed week
     * @param startedProgram The program with the next week to start
     * @return NextWorkoutDTO with the first workout of the next week
     */
    private NextWorkoutDTO startNextWeek(StartedProgram startedProgram) {
        Program program = startedProgram.getProgram();
        List<Week> programWeeks = program.getWeeks();

        // Collect IDs of all weeks that have already been started
        java.util.Set<Long> startedWeekIds = startedProgram.getStartedWeeks().stream()
                .map(StartedWeek::getWeekId)
                .collect(Collectors.toSet());

        // Find the first week in the program that hasn't been started yet
        Optional<Week> nextWeek = programWeeks.stream()
                .filter(week -> !startedWeekIds.contains(week.getId()))
                .findFirst();

        if (nextWeek.isPresent()) {
            // Create a new started week
            StartedWeek newStartedWeek = new StartedWeek();
            newStartedWeek.setWeekId(nextWeek.get().getId());
            newStartedWeek.setStartDate(new Date());
            newStartedWeek.setFinished(false);
            newStartedWeek.setStartedWorkouts(new ArrayList<>());
            startedWeekRepository.save(newStartedWeek);

            // Add to the started program
            startedProgram.getStartedWeeks().add(newStartedWeek);
            startedProgramRepository.save(startedProgram);

            // Get the first workout of the next week
            if (nextWeek.get().getWorkouts().isEmpty()) {
                return NextWorkoutDTO.builder()
                        .message("No workouts defined for the next week")
                        .build();
            }

            Workout firstWorkout = nextWeek.get().getWorkouts().getFirst();

            return NextWorkoutDTO.builder()
                    .nextWorkoutDetails(createNextWorkoutDetailsDTO(firstWorkout))
                    .action("start")
                    .build();
        } else {
            // No more weeks to start, program is completed
            startedProgram.setFinished(true);
            startedProgram.setDoneDate(new Date());
            startedProgramRepository.save(startedProgram);

            return NextWorkoutDTO.builder()
                    .message("Program completed")
                    .build();
        }
    }

    /**
     * Processes a complete set request
     * @param doneSetDTO The completed set data
     * @return Updated StartedWorkout or null if not found
     */
    public NextWorkoutDoneSetDTO completeSet(CreateDoneSetDTO doneSetDTO) {
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(doneSetDTO.getStartedWorkoutId()).orElseThrow();

        DoneSet doneSet = new DoneSet();
        WorkoutExercise workoutExercise = workoutExerciseRepository.findById(doneSetDTO.getWorkoutExerciseId()).orElseThrow();
        Set set = setRepository.findById(doneSetDTO.getSetId()).orElseThrow();
        doneSet.setSet(set);
        doneSet.setWorkoutExercise(workoutExercise);
        doneSet.setVolume(doneSetDTO.getVolume());
        doneSet.setIntensity(doneSetDTO.getIntensity());
        doneSet.setWeightLifted(doneSetDTO.getWeightLifted());
        doneSet.setDate(new Date());
        doneSet = doneSetRepository.save(doneSet);

        // Add the done set to the workout
        startedWorkout.getDoneSets().add(doneSet);
        startedWorkoutRepository.save(startedWorkout);

        // Update workout status based on set completion
        return mapDoneSetToNextWorkoutDoneSetDTO(doneSet);
    }

    public CompleteWorkoutResponseDTO completeWorkout(Long startedWorkoutId, Long startedProgramId) {
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElseThrow();
        StartedProgram startedProgram = startedProgramRepository.findById(startedProgramId).orElseThrow();

        // Find the started week for this workout
        StartedWeek startedWeek =
                startedProgram.getStartedWeeks().stream()
                        .filter(week -> week.getStartedWorkouts().stream()
                                .anyMatch(workout -> workout.getId().equals(startedWorkoutId)))
                        .findFirst().orElseThrow();

        // Mark workout as finished
        startedWorkout.setFinished(true);
        startedWorkout.setDoneDate(new Date());
        startedWorkout = startedWorkoutRepository.save(startedWorkout);

        // Check if this was the last workout in the week
        checkWeekCompletion(startedWeek, startedProgram.getProgram());

        // Check if this was the last week in the program
        if (startedWeek.isFinished()) {
            checkProgramCompletion(startedProgram);
        }

        return new CompleteWorkoutResponseDTO(startedWorkoutId, "Successfully completed workout!");
    }

    /**
     * Checks if all workouts in a week have been completed
     * @param startedWeek The week to check
     * @param program The program containing the week
     */
    private void checkWeekCompletion(StartedWeek startedWeek, Program program) {
        // Get the original week from program using weekId
        Week originalWeek = program.getWeeks().stream()
                .filter(w -> w.getId().equals(startedWeek.getWeekId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Week not found in program"));

        List<StartedWorkout> startedWorkouts = startedWeek.getStartedWorkouts();

        // Collect IDs of all workouts that have been started and finished
        java.util.Set<Long> finishedWorkoutIds = startedWorkouts.stream()
                .filter(StartedWorkout::isFinished)
                .map(StartedWorkout::getWorkoutId)
                .collect(Collectors.toSet());

        // Check if all workouts in the original week have been started and finished
        boolean allWorkoutsFinished = originalWeek.getWorkouts().stream()
                .allMatch(workout -> finishedWorkoutIds.contains(workout.getId()));

        if (allWorkoutsFinished) {
            startedWeek.setFinished(true);
            startedWeek.setDoneDate(new Date());
            startedWeekRepository.save(startedWeek);
        }
    }

    /**
     * Checks if all weeks in a program have been completed
     * @param startedProgram The program to check
     */
    private void checkProgramCompletion(StartedProgram startedProgram) {
        Program program = startedProgram.getProgram();
        List<Week> programWeeks = program.getWeeks();
        List<StartedWeek> startedWeeks = startedProgram.getStartedWeeks();

        // Collect IDs of all weeks that have been started and finished
        java.util.Set<Long> finishedWeekIds = startedWeeks.stream()
                .filter(StartedWeek::isFinished)
                .map(StartedWeek::getWeekId)
                .collect(Collectors.toSet());

        // Check if all weeks in the program have been started and finished
        boolean allWeeksFinished = programWeeks.stream()
                .allMatch(week -> finishedWeekIds.contains(week.getId()));

        if (allWeeksFinished) {
            startedProgram.setFinished(true);
            startedProgram.setDoneDate(new Date());
            startedProgramRepository.save(startedProgram);
        }
    }

    /**
     * Uncompletes a previously completed set
     * @param doneSetId The ID of the completed set to remove
     * @param startedWorkoutId The ID of the started workout containing the set
     * @return Response with success message
     */
    public CompleteSetResponseDTO uncompleteSet(Long doneSetId, Long startedWorkoutId) {
        DoneSet doneSet = doneSetRepository.findById(doneSetId).orElseThrow();
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElseThrow();
        startedWorkout.getDoneSets().remove(doneSet);
        startedWorkoutRepository.save(startedWorkout);
        doneSetRepository.delete(doneSet);

        return new CompleteSetResponseDTO(doneSetId, "Successfully removed completed set from workout!");
    }

    /**
     * Maps a StartedWorkout to NextWorkoutDetailsDTO
     * For mapping already started workouts
     */
    private NextWorkoutDetailsDTO mapStartedWorkoutToNextWorkoutDetailsDTO(StartedWorkout startedWorkout) {
        // Build NextWorkoutDetailsDTO using the builder pattern
        return NextWorkoutDetailsDTO.builder()
                .id(startedWorkout.getId())
                .workoutId(startedWorkout.getWorkoutId())
                .title(startedWorkout.getTitle())
                .description(startedWorkout.getDescription())
                .number(startedWorkout.getNumber())
                .workoutExercises(programService.mapWorkoutExercisesToDetailsDTOs(startedWorkout.getWorkoutExercises()))
                .doneSets(mapDoneSetsToNextWorkoutDoneSetDTOs(startedWorkout.getDoneSets()))
                .startDate(startedWorkout.getStartDate())
                .doneDate(startedWorkout.getDoneDate())
                .finished(startedWorkout.isFinished())
                .build();
    }

    /**
     * Maps a list of DoneSets to NextWorkoutDoneSetDTOs
     */
    private List<NextWorkoutDoneSetDTO> mapDoneSetsToNextWorkoutDoneSetDTOs(List<DoneSet> doneSets) {
        if (doneSets == null) {
            return new ArrayList<>();
        }

        return doneSets.stream()
                .map(this::mapDoneSetToNextWorkoutDoneSetDTO)
                .collect(Collectors.toList());
    }

    /**
     * Maps a DoneSet to NextWorkoutDoneSetDTO
     */
    private NextWorkoutDoneSetDTO mapDoneSetToNextWorkoutDoneSetDTO(DoneSet doneSet) {
        return NextWorkoutDoneSetDTO.builder()
                .id(doneSet.getId())
                .set(mapSetToNextWorkoutSetDefinitionDTO(doneSet.getSet()))
                .workoutExercise(mapWorkoutExerciseToNextWorkoutExerciseDefinitionDTO(doneSet.getWorkoutExercise()))
                .volume(doneSet.getVolume())
                .intensity(doneSet.getIntensity())
                .date(doneSet.getDate())
                .weightLifted(doneSet.getWeightLifted())
                .build();
    }

    /**
     * Maps a Set to NextWorkoutSetDefinitionDTO
     */
    private NextWorkoutSetDefinitionDTO mapSetToNextWorkoutSetDefinitionDTO(Set set) {
        if (set == null) {
            return null;
        }

        return NextWorkoutSetDefinitionDTO.builder()
                .id(set.getId())
                .volume(mapSetVolumeToNextWorkoutSetVolumeDetailsDTO(set.getVolume()))
                .intensity(mapSetIntensityToNextWorkoutSetIntensityDetailsDTO(set.getIntensity()))
                .volumeMetric(mapVolumeMetricToNextWorkoutVolumeMetricInfoDTO(set.getVolumeMetric()))
                .intensityMetric(mapIntensityMetricToNextWorkoutIntensityMetricInfoDTO(set.getIntensityMetric()))
                .build();
    }

    /**
     * Maps a WorkoutExercise to NextWorkoutExerciseDefinitionDTO
     * Note: This is a simplified version - in practice you'd use programService.mapWorkoutExercisesToDetailsDTOs
     */
    private NextWorkoutExerciseDefinitionDTO mapWorkoutExerciseToNextWorkoutExerciseDefinitionDTO(WorkoutExercise workoutExercise) {
        if (workoutExercise == null) {
            return null;
        }

        return NextWorkoutExerciseDefinitionDTO.builder()
                .id(workoutExercise.getId())
                .exercise(mapExerciseToNextWorkoutExerciseBaseDTO(workoutExercise.getExercise()))
                .sets(workoutExercise.getSets().stream()
                        .map(this::mapSetToNextWorkoutSetDefinitionDTO)
                        .collect(Collectors.toList()))
                .minimumRestTime(workoutExercise.getMinimumRestTime())
                .maximumRestTime(workoutExercise.getMaximumRestTime())
                .build();
    }

    /**
     * Maps an Exercise to NextWorkoutExerciseBaseDTO
     */
    private NextWorkoutExerciseBaseDTO mapExerciseToNextWorkoutExerciseBaseDTO(Exercise exercise) {
        if (exercise == null) {
            return null;
        }

        return NextWorkoutExerciseBaseDTO.builder()
                .id(exercise.getId())
                .title(exercise.getTitle())
                .description(exercise.getDescription())
                .exerciseMuscles(exercise.getExerciseMuscles().stream()
                        .map(this::mapExerciseMuscleToNextWorkoutExerciseMuscleLinkDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Maps an ExerciseMuscle to NextWorkoutExerciseMuscleLinkDTO
     */
    private NextWorkoutExerciseMuscleLinkDTO mapExerciseMuscleToNextWorkoutExerciseMuscleLinkDTO(ExerciseMuscle exerciseMuscle) {
        if (exerciseMuscle == null) {
            return null;
        }

        return NextWorkoutExerciseMuscleLinkDTO.builder()
                .id(exerciseMuscle.getId())
                .muscle(mapMuscleToNextWorkoutMuscleInfoDTO(exerciseMuscle.getMuscle()))
                .intensity(exerciseMuscle.getIntensity())
                .build();
    }

    /**
     * Maps a Muscle to NextWorkoutMuscleInfoDTO
     */
    private NextWorkoutMuscleInfoDTO mapMuscleToNextWorkoutMuscleInfoDTO(Muscle muscle) {
        if (muscle == null) {
            return null;
        }

        return NextWorkoutMuscleInfoDTO.builder()
                .id(muscle.getId())
                .name(muscle.getName())
                .build();
    }

    /**
     * Maps a SetVolume to NextWorkoutSetVolumeDetailsDTO
     */
    private NextWorkoutSetVolumeDetailsDTO mapSetVolumeToNextWorkoutSetVolumeDetailsDTO(SetVolume volume) {
        if (volume == null) {
            return null;
        }

        return new NextWorkoutSetVolumeDetailsDTO(
                volume.getMinimumVolume(),
                volume.getMaximumVolume()
        );
    }

    /**
     * Maps a SetIntensity to NextWorkoutSetIntensityDetailsDTO
     */
    private NextWorkoutSetIntensityDetailsDTO mapSetIntensityToNextWorkoutSetIntensityDetailsDTO(SetIntensity intensity) {
        if (intensity == null) {
            return null;
        }

        return new NextWorkoutSetIntensityDetailsDTO(
                intensity.getMinimumIntensity(),
                intensity.getMaximumIntensity()
        );
    }

    /**
     * Maps a VolumeMetric to NextWorkoutVolumeMetricInfoDTO
     */
    private NextWorkoutVolumeMetricInfoDTO mapVolumeMetricToNextWorkoutVolumeMetricInfoDTO(VolumeMetric volumeMetric) {
        if (volumeMetric == null) {
            return null;
        }

        return NextWorkoutVolumeMetricInfoDTO.builder()
                .id(volumeMetric.getId())
                .isRange(volumeMetric.isRange())
                .title(volumeMetric.getTitle())
                .metricSymbol(volumeMetric.getMetricSymbol())
                .build();
    }

    /**
     * Maps an IntensityMetric to NextWorkoutIntensityMetricInfoDTO
     */
    private NextWorkoutIntensityMetricInfoDTO mapIntensityMetricToNextWorkoutIntensityMetricInfoDTO(IntensityMetric intensityMetric) {
        if (intensityMetric == null) {
            return null;
        }

        return NextWorkoutIntensityMetricInfoDTO.builder()
                .id(intensityMetric.getId())
                .minimumIntensity(intensityMetric.getMinimumIntensity())
                .maximumIntensity(intensityMetric.getMaximumIntensity())
                .isRange(intensityMetric.isRange())
                .title(intensityMetric.getTitle())
                .metricSymbol(intensityMetric.getMetricSymbol())
                .build();
    }
}