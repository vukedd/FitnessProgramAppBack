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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
            Workout workoutToStart = nextWorkoutDTO.getNextWorkoutDetails().getWorkout().getId() != null ?
                    workoutRepository.findById(nextWorkoutDTO.getNextWorkoutDetails().getWorkout().getId()).orElseThrow():null;

            // Create and persist a real started workout instance
            StartedWorkout newStartedWorkout = new StartedWorkout();
            newStartedWorkout.setWorkout(workoutToStart);
            newStartedWorkout.setStartDate(new Date());
            newStartedWorkout.setFinished(false);

            // Save the new started workout
            StartedWorkout savedWorkout = startedWorkoutRepository.save(newStartedWorkout);

            // Add it to the latest started week
            startedProgram.getStartedWeeks().getLast()
                    .getStartedWorkouts().add(savedWorkout);

            // Save the started week
            startedWeekRepository.save(startedProgram.getStartedWeeks().get(startedProgram.getStartedWeeks().size() - 1));

            // Save the program
            startedProgramRepository.save(startedProgram);

            // Update the DTO with the persisted started workout
            nextWorkoutDTO.setNextWorkoutDetails(mapStartedWorkoutToNextWorkoutDetailsDTO(savedWorkout));
        }

        return nextWorkoutDTO;
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

        // Get the last started week
        List<StartedWeek> startedWeeks = startedProgram.getStartedWeeks();

        if (startedWeeks.isEmpty()) {
            // No weeks started yet, start the first week of the program
            return startFirstWeek(startedProgram);
        }

        // Sort weeks by ID to ensure we get the latest one
        startedWeeks.sort(Comparator.comparing(StartedWeek::getId));
        StartedWeek latestStartedWeek = startedWeeks.get(startedWeeks.size() - 1);

        if (!latestStartedWeek.isFinished()) {
            // Week is not finished, look for the next workout to do in this week
            return getNextWorkoutInWeek(latestStartedWeek);
        } else {
            // Current week is finished, start next week
            return startNextWeek(startedProgram, latestStartedWeek);
        }
    }

    /**
     * Gets the next workout to do in the current week
     * @param startedWeek The current week
     * @return NextWorkoutDTO with the next workout and action
     */
    private NextWorkoutDTO getNextWorkoutInWeek(StartedWeek startedWeek) {
        List<StartedWorkout> startedWorkouts = startedWeek.getStartedWorkouts();

        if (startedWorkouts.isEmpty()) {
            // No workouts started in this week, start the first workout
            Week week = startedWeek.getWeek();
            if (week.getWorkouts().isEmpty()) {
                return NextWorkoutDTO.builder()
                        .message("No workouts defined for this week")
                        .build();
            }

            // Create a non-persisted StartedWorkout with the workout to start
            Workout nextWorkout = week.getWorkouts().get(0);
            StartedWorkout tempStartedWorkout = new StartedWorkout();
            tempStartedWorkout.setWorkout(nextWorkout);

            return NextWorkoutDTO.builder()
                    .nextWorkoutDetails(mapStartedWorkoutToNextWorkoutDetailsDTO(tempStartedWorkout))
                    .action("start")
                    .build();
        }

        // Sort workouts by ID to get the latest one
        startedWorkouts.sort(Comparator.comparing(StartedWorkout::getId));
        StartedWorkout latestWorkout = startedWorkouts.getLast();

        if (!latestWorkout.isFinished()) {
            // Latest workout is not finished, continue it
            return NextWorkoutDTO.builder()
                    .nextWorkoutDetails(mapStartedWorkoutToNextWorkoutDetailsDTO(latestWorkout))
                    .action("continue")
                    .build();
        }

        // Latest workout is finished, find the next one in the week
        Week week = startedWeek.getWeek();
        List<Workout> weekWorkouts = week.getWorkouts();

        // Find the index of the current workout in the week's workout list
        int currentWorkoutIndex = -1;
        for (int i = 0; i < weekWorkouts.size(); i++) {
            if (weekWorkouts.get(i).getId().equals(latestWorkout.getWorkout().getId())) {
                currentWorkoutIndex = i;
                break;
            }
        }

        // Check if there's a next workout in this week
        if (currentWorkoutIndex < weekWorkouts.size() - 1) {
            Workout nextWorkout = weekWorkouts.get(currentWorkoutIndex + 1);

            // Create a non-persisted StartedWorkout with the workout to start
            StartedWorkout tempStartedWorkout = new StartedWorkout();
            tempStartedWorkout.setWorkout(nextWorkout);

            return NextWorkoutDTO.builder()
                    .nextWorkoutDetails(mapStartedWorkoutToNextWorkoutDetailsDTO(tempStartedWorkout))
                    .action("start")
                    .build();
        }

        // No more workouts in this week, mark week as finished
        startedWeek.setFinished(true);
        startedWeek.setDoneDate(new Date());
        return NextWorkoutDTO.builder()
                .message("Week completed")
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
        Week firstWeek = programWeeks.get(0);

        // Create a new started week
        StartedWeek newStartedWeek = new StartedWeek();
        newStartedWeek.setWeek(firstWeek);
        newStartedWeek.setStartDate(new Date());
        newStartedWeek.setFinished(false);
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

        // Create a non-persisted StartedWorkout with the workout to start
        StartedWorkout tempStartedWorkout = new StartedWorkout();
        tempStartedWorkout.setWorkout(firstWorkout);

        return NextWorkoutDTO.builder()
                .nextWorkoutDetails(mapStartedWorkoutToNextWorkoutDetailsDTO(tempStartedWorkout))
                .action("start")
                .build();
    }

    /**
     * Starts the next week after a completed week
     * @param startedProgram The program
     * @param completedStartedWeek The completed week
     * @return NextWorkoutDTO with the first workout of the next week
     */
    private NextWorkoutDTO startNextWeek(StartedProgram startedProgram, StartedWeek completedStartedWeek) {
        Program program = startedProgram.getProgram();
        List<Week> programWeeks = program.getWeeks();

        // Find the index of the completed week in the program's week list
        int completedWeekIndex = -1;
        Week completedWeek = completedStartedWeek.getWeek();

        for (int i = 0; i < programWeeks.size(); i++) {
            if (programWeeks.get(i).getId().equals(completedWeek.getId())) {
                completedWeekIndex = i;
                break;
            }
        }

        // Check if there's a next week in the program
        if (completedWeekIndex < programWeeks.size() - 1) {
            // There's a next week, start it
            Week nextWeek = programWeeks.get(completedWeekIndex + 1);

            // Create a new started week
            StartedWeek newStartedWeek = new StartedWeek();
            newStartedWeek.setWeek(nextWeek);
            newStartedWeek.setStartDate(new Date());
            newStartedWeek.setFinished(false);
            startedWeekRepository.save(newStartedWeek);

            // Add to the started program
            startedProgram.getStartedWeeks().add(newStartedWeek);
            startedProgramRepository.save(startedProgram);

            // Get the first workout of the next week
            if (nextWeek.getWorkouts().isEmpty()) {
                return NextWorkoutDTO.builder()
                        .message("No workouts defined for the next week")
                        .build();
            }

            Workout firstWorkout = nextWeek.getWorkouts().get(0);

            // Create a non-persisted StartedWorkout with the workout to start
            StartedWorkout tempStartedWorkout = new StartedWorkout();
            tempStartedWorkout.setWorkout(firstWorkout);

            return NextWorkoutDTO.builder()
                    .nextWorkoutDetails(mapStartedWorkoutToNextWorkoutDetailsDTO(tempStartedWorkout))
                    .action("start")
                    .build();
        } else {
            // No more weeks, program is completed
            startedProgram.setFinished(true);
            startedProgram.setDoneDate(new Date());
            startedProgramRepository.save(startedProgram);
            return NextWorkoutDTO.builder()
                    .message("Program completed")
                    .build();
        }
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
        startedWorkout=startedWorkoutRepository.save(startedWorkout);

        // Check if this was the last workout in the week
        checkWeekCompletion(startedWeek);

        // Check if this was the last week in the program
        if (startedWeek.isFinished()) {
            checkProgramCompletion(startedProgram);
        }

        return new CompleteWorkoutResponseDTO(startedWorkoutId,"Successfully completed workout!");
    }

    /**
     * Checks if all workouts in a week have been completed
     * @param startedWeek The week to check
     */
    private void checkWeekCompletion(StartedWeek startedWeek) {
        Week week = startedWeek.getWeek();
        List<Workout> weekWorkouts = week.getWorkouts();
        List<StartedWorkout> startedWorkouts = startedWeek.getStartedWorkouts();

        // Check if we have started and finished all workouts in the week
        if (startedWorkouts.size() == weekWorkouts.size()) {
            boolean allWorkoutsFinished = true;

            for (StartedWorkout startedWorkout : startedWorkouts) {
                if (!startedWorkout.isFinished()) {
                    allWorkoutsFinished = false;
                    break;
                }
            }

            if (allWorkoutsFinished) {
                startedWeek.setFinished(true);
                startedWeek.setDoneDate(new Date());
                startedWeekRepository.save(startedWeek);
            }
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

        // Check if we have started and finished all weeks in the program
        if (startedWeeks.size() == programWeeks.size()) {
            boolean allWeeksFinished = true;

            for (StartedWeek startedWeek : startedWeeks) {
                if (!startedWeek.isFinished()) {
                    allWeeksFinished = false;
                    break;
                }
            }

            if (allWeeksFinished) {
                startedProgram.setFinished(true);
                startedProgram.setDoneDate(new Date());
                startedProgramRepository.save(startedProgram);
            }
        }
    }

    /**
     * Uncompletes a previously completed set
     * @param doneSetId The ID of the completed set to remove
     * @return Response with success message
     */
    public CompleteSetResponseDTO uncompleteSet(Long doneSetId,Long startedWorkoutId) {
        DoneSet doneSet = doneSetRepository.findById(doneSetId).orElseThrow();
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElseThrow();
        startedWorkout.getDoneSets().remove(doneSet);
        startedWorkoutRepository.save(startedWorkout);
        doneSetRepository.delete(doneSet);

        return new CompleteSetResponseDTO(doneSetId, "Successfully removed completed set from workout!");
    }

    private NextWorkoutDetailsDTO mapStartedWorkoutToNextWorkoutDetailsDTO(StartedWorkout entity) {
        if (entity == null) return null;
        return NextWorkoutDetailsDTO.builder() // Use new DTO name
                .id(entity.getId())
                .workout(mapWorkoutToNextWorkoutDefinitionDTO(entity.getWorkout())) // Call renamed mapper
                .doneSets(mapDoneSetListToNextWorkoutDoneSetDTOList(entity.getDoneSets())) // Call renamed list mapper
                .startDate(entity.getStartDate())
                .doneDate(entity.getDoneDate())
                .finished(entity.isFinished())
                .build();
    }

    // Renamed: mapWorkoutToDTO -> mapWorkoutToNextWorkoutDefinitionDTO
    // Returns: NextWorkoutDefinitionDTO
    private NextWorkoutDefinitionDTO mapWorkoutToNextWorkoutDefinitionDTO(Workout entity) {
        if (entity == null) return null;
        return NextWorkoutDefinitionDTO.builder() // Use new DTO name
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .number(entity.getNumber())
                .workoutExercises(mapWorkoutExerciseListToNextWorkoutExerciseDefinitionDTOList(entity.getWorkoutExercises())) // Call renamed list mapper
                .build();
    }

    // Renamed list mapper
    private List<NextWorkoutDoneSetDTO> mapDoneSetListToNextWorkoutDoneSetDTOList(List<DoneSet> list) {
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(this::mapDoneSetToNextWorkoutDoneSetDTO) // Call renamed item mapper
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Renamed list mapper
    private List<NextWorkoutExerciseDefinitionDTO> mapWorkoutExerciseListToNextWorkoutExerciseDefinitionDTOList(List<WorkoutExercise> list) {
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(this::mapWorkoutExerciseToNextWorkoutExerciseDefinitionDTO) // Call renamed item mapper
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Renamed: mapDoneSetToDTO -> mapDoneSetToNextWorkoutDoneSetDTO
    // Returns: NextWorkoutDoneSetDTO
    private NextWorkoutDoneSetDTO mapDoneSetToNextWorkoutDoneSetDTO(DoneSet entity) {
        if (entity == null) return null;
        return NextWorkoutDoneSetDTO.builder() // Use new DTO name
                .id(entity.getId())
                .set(mapSetToNextWorkoutSetDefinitionDTO(entity.getSet())) // Call renamed mapper
                .workoutExercise(mapWorkoutExerciseToNextWorkoutExerciseDefinitionDTO(entity.getWorkoutExercise())) // Call renamed mapper
                .volume(entity.getVolume())
                .intensity(entity.getIntensity())
                .date(entity.getDate())
                .weightLifted(entity.getWeightLifted())
                .build();
    }

    // Renamed: mapWorkoutExerciseToDTO -> mapWorkoutExerciseToNextWorkoutExerciseDefinitionDTO
    // Returns: NextWorkoutExerciseDefinitionDTO
    private NextWorkoutExerciseDefinitionDTO mapWorkoutExerciseToNextWorkoutExerciseDefinitionDTO(WorkoutExercise entity) {
        if (entity == null) return null;
        return NextWorkoutExerciseDefinitionDTO.builder() // Use new DTO name
                .id(entity.getId())
                .exercise(mapExerciseToNextWorkoutExerciseBaseDTO(entity.getExercise())) // Call renamed mapper
                .sets(mapSetListToNextWorkoutSetDefinitionDTOList(entity.getSets())) // Call renamed list mapper
                .minimumRestTime(entity.getMinimumRestTime())
                .maximumRestTime(entity.getMaximumRestTime())
                .build();
    }

    // Renamed: mapExerciseToDTO -> mapExerciseToNextWorkoutExerciseBaseDTO
    // Returns: NextWorkoutExerciseBaseDTO
    private NextWorkoutExerciseBaseDTO mapExerciseToNextWorkoutExerciseBaseDTO(Exercise entity) {
        if (entity == null) return null;
        return NextWorkoutExerciseBaseDTO.builder() // Use new DTO name
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .exerciseMuscles(mapExerciseMuscleListToNextWorkoutExerciseMuscleLinkDTOList(entity.getExerciseMuscles())) // Call renamed list mapper
                .build();
    }

    // Renamed list mapper
    private List<NextWorkoutSetDefinitionDTO> mapSetListToNextWorkoutSetDefinitionDTOList(List<Set> list) {
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(this::mapSetToNextWorkoutSetDefinitionDTO) // Call renamed item mapper
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Renamed list mapper
    private List<NextWorkoutExerciseMuscleLinkDTO> mapExerciseMuscleListToNextWorkoutExerciseMuscleLinkDTOList(List<ExerciseMuscle> list) {
        if (list == null) return Collections.emptyList();
        return list.stream()
                .map(this::mapExerciseMuscleToNextWorkoutExerciseMuscleLinkDTO) // Call renamed item mapper
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Renamed: mapExerciseMuscleToDTO -> mapExerciseMuscleToNextWorkoutExerciseMuscleLinkDTO
    // Returns: NextWorkoutExerciseMuscleLinkDTO
    private NextWorkoutExerciseMuscleLinkDTO mapExerciseMuscleToNextWorkoutExerciseMuscleLinkDTO(ExerciseMuscle entity) {
        if (entity == null) return null;
        return NextWorkoutExerciseMuscleLinkDTO.builder() // Use new DTO name
                .id(entity.getId())
                .muscle(mapMuscleToNextWorkoutMuscleInfoDTO(entity.getMuscle())) // Call renamed mapper
                .intensity(entity.getIntensity())
                .build();
    }

    // Renamed: mapMuscleToDTO -> mapMuscleToNextWorkoutMuscleInfoDTO
    // Returns: NextWorkoutMuscleInfoDTO
    private NextWorkoutMuscleInfoDTO mapMuscleToNextWorkoutMuscleInfoDTO(Muscle entity) {
        if (entity == null) return null;
        return NextWorkoutMuscleInfoDTO.builder() // Use new DTO name
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    // Renamed: mapSetToDTO -> mapSetToNextWorkoutSetDefinitionDTO
    // Returns: NextWorkoutSetDefinitionDTO
    private NextWorkoutSetDefinitionDTO mapSetToNextWorkoutSetDefinitionDTO(Set entity) {
        if (entity == null) return null;
        return NextWorkoutSetDefinitionDTO.builder() // Use new DTO name
                .id(entity.getId())
                .volume(mapSetVolumeToNextWorkoutSetVolumeDetailsDTO(entity.getVolume())) // Call renamed mapper
                .intensity(mapSetIntensityToNextWorkoutSetIntensityDetailsDTO(entity.getIntensity())) // Call renamed mapper
                .volumeMetric(mapVolumeMetricToNextWorkoutVolumeMetricInfoDTO(entity.getVolumeMetric())) // Call renamed mapper
                .intensityMetric(mapIntensityMetricToNextWorkoutIntensityMetricInfoDTO(entity.getIntensityMetric())) // Call renamed mapper
                .build();
    }

    // Renamed: mapSetVolumeToDTO -> mapSetVolumeToNextWorkoutSetVolumeDetailsDTO
    // Returns: NextWorkoutSetVolumeDetailsDTO
    private NextWorkoutSetVolumeDetailsDTO mapSetVolumeToNextWorkoutSetVolumeDetailsDTO(SetVolume embeddable) {
        if (embeddable == null) return null;
        // Use new DTO name for constructor/return type
        return new NextWorkoutSetVolumeDetailsDTO(embeddable.getMinimumVolume(), embeddable.getMaximumVolume());
    }

    // Renamed: mapSetIntensityToDTO -> mapSetIntensityToNextWorkoutSetIntensityDetailsDTO
    // Returns: NextWorkoutSetIntensityDetailsDTO
    private NextWorkoutSetIntensityDetailsDTO mapSetIntensityToNextWorkoutSetIntensityDetailsDTO(SetIntensity embeddable) {
        if (embeddable == null) return null;
        // Use new DTO name for constructor/return type
        return new NextWorkoutSetIntensityDetailsDTO(embeddable.getMinimumIntensity(), embeddable.getMaximumIntensity());
    }

    // Renamed: mapVolumeMetricToDTO -> mapVolumeMetricToNextWorkoutVolumeMetricInfoDTO
    // Returns: NextWorkoutVolumeMetricInfoDTO
    private NextWorkoutVolumeMetricInfoDTO mapVolumeMetricToNextWorkoutVolumeMetricInfoDTO(VolumeMetric entity) {
        if (entity == null) return null;
        return NextWorkoutVolumeMetricInfoDTO.builder() // Use new DTO name
                .id(entity.getId())
                .isRange(entity.isRange())
                .title(entity.getTitle())
                .metricSymbol(entity.getMetricSymbol())
                .build();
    }

    // Renamed: mapIntensityMetricToDTO -> mapIntensityMetricToNextWorkoutIntensityMetricInfoDTO
    // Returns: NextWorkoutIntensityMetricInfoDTO
    private NextWorkoutIntensityMetricInfoDTO mapIntensityMetricToNextWorkoutIntensityMetricInfoDTO(IntensityMetric entity) {
        if (entity == null) return null;
        return NextWorkoutIntensityMetricInfoDTO.builder() // Use new DTO name
                .id(entity.getId())
                .minimumIntensity(entity.getMinimumIntensity())
                .maximumIntensity(entity.getMaximumIntensity())
                .isRange(entity.isRange())
                .title(entity.getTitle())
                .metricSymbol(entity.getMetricSymbol())
                .build();
    }
}