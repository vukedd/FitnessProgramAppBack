package com.app.fitness.fitnesprogramapp.services.workout;

import com.app.fitness.fitnesprogramapp.dtos.exercise.ChangeExerciseOrderDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.CreateSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.details.WorkoutExerciseDetailsDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateDoneSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.set.AddSetDTO;
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
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.models.week.StartedWeek;
import com.app.fitness.fitnesprogramapp.models.week.Week;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import com.app.fitness.fitnesprogramapp.repositories.exercise.ExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.exercise.WorkoutExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.program.ProgramRepository;
import com.app.fitness.fitnesprogramapp.repositories.startedprogram.StartedProgramRepository;
import com.app.fitness.fitnesprogramapp.repositories.set.DoneSetRepository;
import com.app.fitness.fitnesprogramapp.repositories.set.SetRepository;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import com.app.fitness.fitnesprogramapp.repositories.week.StartedWeekRepository;
import com.app.fitness.fitnesprogramapp.repositories.workout.StartedWorkoutRepository;
import com.app.fitness.fitnesprogramapp.repositories.workout.WorkoutRepository;
import com.app.fitness.fitnesprogramapp.services.program.ProgramService;
import jakarta.transaction.Transactional;
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
    private final ProgramService programService; // For workout exercise mapping
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final ProgramRepository programRepository;

    @Transactional
    public NextWorkoutDTO processNextWorkout(Long programId,String username) {
        User user=userRepository.findByUsername(username).orElseThrow();
        Optional<StartedProgram> startedProgramOpt = user.getStartedPrograms().stream().filter(
                sp->sp.getId().equals(programId)
        ).findFirst();

        if (startedProgramOpt.isEmpty()) {
            return null;
        }

        StartedProgram startedProgram = startedProgramOpt.get();
        NextWorkoutDTO nextWorkoutDTO = getNextWorkout(startedProgram);

        if (nextWorkoutDTO.getNextWorkoutDetails() != null && "start".equals(nextWorkoutDTO.getAction())) {
            Workout workoutToStart = workoutRepository.findAndInitializeById(nextWorkoutDTO.getNextWorkoutDetails().getWorkoutId())
                    .orElseThrow(() -> new RuntimeException("Workout not found"));

            StartedWorkout newStartedWorkout = createStartedWorkoutFromWorkout(workoutToStart);
            newStartedWorkout.setStartDate(new Date());
            newStartedWorkout.setFinished(false);


            StartedWorkout savedStartedWorkout = startedWorkoutRepository.save(newStartedWorkout);
            StartedWeek latestStartedWeek = startedProgram.getStartedWeeks().getLast();
            latestStartedWeek.getStartedWorkouts().add(savedStartedWorkout);
            startedProgramRepository.save(startedProgram);

            nextWorkoutDTO.setNextWorkoutDetails(mapStartedWorkoutToNextWorkoutDetailsDTO(newStartedWorkout));



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
        startedWorkout.setWorkoutId(workout.getId()); // Store the original workout ID

        startedWorkout.setWorkoutExercises(new ArrayList<>());
        for(WorkoutExercise workoutExercise: workout.getWorkoutExercises()) {
            WorkoutExercise exercise = new WorkoutExercise();
            exercise.setExercise(workoutExercise.getExercise());
            exercise.setMaximumRestTime(workoutExercise.getMaximumRestTime());
            exercise.setMinimumRestTime(workoutExercise.getMinimumRestTime());
            exercise.setPosition(workoutExercise.getPosition());
            exercise.setSets(new ArrayList<>());
            for(Set set: workoutExercise.getSets()) {
                Set s = new Set();
                s.setWorkoutExercise(exercise);
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
                exercise.getSets().add(s);
            }
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
        Program program=programRepository.findAndInitializeById(startedProgram.getProgramId()).orElse(null);

        // Get the current started weeks
        List<StartedWeek> startedWeeks = startedProgram.getStartedWeeks();

        if (startedWeeks.isEmpty()) {
            // No weeks started yet, start the first week of the program
            return startFirstWeek(startedProgram,program);
        }

        // Sort weeks by ID to ensure we get the latest one
        startedWeeks.sort(Comparator.comparing(StartedWeek::getId));
        StartedWeek latestStartedWeek = startedWeeks.getLast();

        if (!latestStartedWeek.isFinished()) {
            return getNextWorkoutInWeek(latestStartedWeek, program);
        } else {
            // Current week is finished, start next week
            return startNextWeek(startedProgram,program);
        }
    }

    /**
     * Gets the next workout to do in the current week
     * @param startedWeek The current week
     * @return NextWorkoutDTO with the next workout and action
     */
    private NextWorkoutDTO getNextWorkoutInWeek(StartedWeek startedWeek, Program program) {
        // Get the original week from program using weekId
        Optional<Week> optionalWeek=Optional.empty();
        if (program!=null) {
            optionalWeek = program.getWeeks().stream()
                    .filter(w -> w.getId().equals(startedWeek.getWeekId()))
                    .findFirst();
        }


        List<Workout> originalWeekWorkouts;
        originalWeekWorkouts = optionalWeek.map(week -> week.getWorkouts().stream().sorted(Comparator.comparing(Workout::getPosition)).toList()).orElseGet(ArrayList::new);

        List<StartedWorkout> startedWorkouts = startedWeek.getStartedWorkouts();

        if (startedWorkouts.isEmpty()) {
            // No workouts started in this week, start the first workout in this week
            if (originalWeekWorkouts.isEmpty()) {
                return NextWorkoutDTO.builder()
                        .message("No workouts defined for this week")
                        .build();
            }

            // Start the first workout of the week
            Workout nextWorkout = originalWeekWorkouts.getFirst();

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
        Optional<Workout> nextWorkout = originalWeekWorkouts.stream()
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
    private NextWorkoutDTO startFirstWeek(StartedProgram startedProgram, Program program) {
        List<Week> programWeeks;
        if (program == null) {
            programWeeks=new ArrayList<>();
        }
        else {
            programWeeks=program.getWeeks();
        }

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

        // Add to the started program
        startedProgram.getStartedWeeks().add(newStartedWeek);

        // Get the first workout of the first week
        if (firstWeek.getWorkouts().isEmpty()) {
            return NextWorkoutDTO.builder()
                    .message("No workouts defined for the first week")
                    .build();
        }

        Workout firstWorkout = firstWeek.getWorkouts().stream().sorted(Comparator.comparing(Workout::getPosition)).toList().getFirst();

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
    private NextWorkoutDTO startNextWeek(StartedProgram startedProgram, Program program) {
        List<Week> programWeeks;
        if (program == null) {
            programWeeks=new ArrayList<>();
        }
        else {
            programWeeks=program.getWeeks();
        }

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

            // Add to the started program
            startedProgram.getStartedWeeks().add(newStartedWeek);

            // Get the first workout of the next week
            if (nextWeek.get().getWorkouts().isEmpty()) {
                return NextWorkoutDTO.builder()
                        .message("No workouts defined for the next week")
                        .build();
            }

            Workout firstWorkout = nextWeek.get().getWorkouts().stream().sorted(Comparator.comparing(Workout::getPosition)).toList().getFirst();


            return NextWorkoutDTO.builder()
                    .nextWorkoutDetails(createNextWorkoutDetailsDTO(firstWorkout))
                    .action("start")
                    .build();
        } else {
            // No more weeks to start, program is completed
            startedProgram.setFinished(true);
            startedProgram.setDoneDate(new Date());

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
    @Transactional
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
        doneSet=doneSetRepository.save(doneSet);

        // Add the done set to the workout
        startedWorkout.getDoneSets().add(doneSet);
        startedWorkoutRepository.save(startedWorkout);

        // Update workout status based on set completion
        return mapDoneSetToNextWorkoutDoneSetDTO(doneSet);
    }

    public CompleteWorkoutResponseDTO completeWorkout(Long startedWorkoutId, Long startedProgramId) {
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElseThrow();
        StartedProgram startedProgram = startedProgramRepository.findById(startedProgramId).orElseThrow();
        Program program=programRepository.findAndInitializeById(startedProgram.getProgramId()).orElse(null);

        // Find the started week for this workout
        StartedWeek startedWeek =
                startedProgram.getStartedWeeks().stream()
                        .filter(week -> week.getStartedWorkouts().stream()
                                .anyMatch(workout -> workout.getId().equals(startedWorkoutId)))
                        .findFirst().orElseThrow();

        // Mark workout as finished
        startedWorkout.setFinished(true);
        startedWorkout.setDoneDate(new Date());

        // Check if this was the last workout in the week

        checkWeekCompletion(startedWeek,program);

        // Check if this was the last week in the program
        if (startedWeek.isFinished()) {
            checkProgramCompletion(startedProgram, program);
        }
        startedProgramRepository.save(startedProgram);

        return new CompleteWorkoutResponseDTO(startedWorkoutId, "Successfully completed workout!");
    }

    /**
     * Checks if all workouts in a week have been completed
     * @param startedWeek The week to check
     */
    private void checkWeekCompletion(StartedWeek startedWeek, Program program) {
        Optional<Week> optionalWeek=Optional.empty();
        if (program!=null) {
            optionalWeek = program.getWeeks().stream()
                    .filter(w -> w.getId().equals(startedWeek.getWeekId()))
                    .findFirst();
        }
        List<Workout> originalWeekWorkouts;
        originalWeekWorkouts = optionalWeek.map(week -> week.getWorkouts().stream().sorted(Comparator.comparing(Workout::getPosition)).toList()).orElseGet(ArrayList::new);

        List<StartedWorkout> startedWorkouts = startedWeek.getStartedWorkouts();

        // Collect IDs of all workouts that have been started and finished
        java.util.Set<Long> finishedWorkoutIds = startedWorkouts.stream()
                .filter(StartedWorkout::isFinished)
                .map(StartedWorkout::getWorkoutId)
                .collect(Collectors.toSet());

        // Check if all workouts in the original week have been started and finished
        boolean allWorkoutsFinished = originalWeekWorkouts.stream()
                .allMatch(workout -> finishedWorkoutIds.contains(workout.getId()));

        if (allWorkoutsFinished) {
            startedWeek.setFinished(true);
            startedWeek.setDoneDate(new Date());
        }
    }

    /**
     * Checks if all weeks in a program have been completed
     * @param startedProgram The program to check
     */
    private void checkProgramCompletion(StartedProgram startedProgram, Program program) {
        List<Week> programWeeks;
        if(program==null) {
            programWeeks=new ArrayList<>();
        }
        else {
            programWeeks=program.getWeeks();
        }
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
        }
    }

    /**
     * Uncompletes a previously completed set
     * @param doneSetId The ID of the completed set to remove
     * @param startedWorkoutId The ID of the started workout containing the set
     * @return Response with success message
     */
    @Transactional
    public CompleteSetResponseDTO uncompleteSet(Long doneSetId, Long startedWorkoutId) {
        DoneSet doneSet = doneSetRepository.findById(doneSetId).orElseThrow();
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElseThrow();
        startedWorkout.getDoneSets().remove(doneSet);
        startedWorkoutRepository.save(startedWorkout);

        return new CompleteSetResponseDTO(doneSetId, "Successfully removed completed set from workout!");
    }

    @Transactional
    public AddSetDTO addSet(Long startedWorkoutId,Long workoutExerciseId, CreateSetDTO createSetDTO) {
        Set newSet=programService.createWorkoutExerciseSet(createSetDTO);
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElseThrow();
        Optional<WorkoutExercise> workoutExercise=startedWorkout.getWorkoutExercises().stream()
                .filter(we->we.getId().equals(workoutExerciseId)).findFirst();
        if(workoutExercise.isEmpty()){
            throw new RuntimeException("Workout exercise not found!");
        }
        workoutExercise.get().getSets().add(newSet);
        startedWorkoutRepository.save(startedWorkout);
        return new AddSetDTO(newSet.getId());
    }

    @Transactional
    public String changeExercise(Long startedWorkoutId,Long workoutExerciseId,Long newExerciseId) {
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElseThrow();
        Optional<WorkoutExercise> workoutExercise=startedWorkout.getWorkoutExercises().stream()
                .filter(we->we.getId().equals(workoutExerciseId)).findFirst();
        if(workoutExercise.isEmpty()){
            throw new RuntimeException("Workout exercise not found!");
        }
        Exercise exercise=exerciseRepository.findById(newExerciseId).orElseThrow();
        workoutExercise.get().setExercise(exercise);
        startedWorkoutRepository.save(startedWorkout);
        return "Successfully changed exercise!";
    }

    @Transactional
    public String changeExerciseOrder(Long startedWorkoutId, ChangeExerciseOrderDTO changeExerciseOrderDTO) {
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElseThrow();

        // Create a map to store the new order for each exercise
        Map<Long, Integer> newOrderMap = new HashMap<>();
        List<Long> exerciseIds = changeExerciseOrderDTO.getWorkoutExerciseIds();

        // Populate the order map (position in list = new order)
        for (int i = 0; i < exerciseIds.size(); i++) {
            newOrderMap.put(exerciseIds.get(i), i);
        }

        // Validate all exercises in the DTO exist in the workout
        java.util.Set<Long> workoutExerciseIdSet = startedWorkout.getWorkoutExercises().stream()
                .map(WorkoutExercise::getId)
                .collect(Collectors.toSet());

        if (!workoutExerciseIdSet.containsAll(exerciseIds) || workoutExerciseIdSet.size() != exerciseIds.size()) {
            throw new IllegalArgumentException("Invalid exercise IDs provided");
        }

        // Update the sort order for each exercise
        for (WorkoutExercise exercise : startedWorkout.getWorkoutExercises()) {
            exercise.setPosition(newOrderMap.get(exercise.getId()));
        }

        // Save the workout with updated sort orders
        startedWorkoutRepository.save(startedWorkout);

        return "Exercise order updated successfully";
    }

    /**
     * Maps a StartedWorkout to NextWorkoutDetailsDTO
     * For mapping already started workouts
     */
    private NextWorkoutDetailsDTO mapStartedWorkoutToNextWorkoutDetailsDTO(StartedWorkout startedWorkout) {
        List<WorkoutExerciseDetailsDTO> sortedExercises = startedWorkout.getWorkoutExercises().stream()
                .sorted(Comparator.comparing(WorkoutExercise::getPosition))
                .map(programService::mapWorkoutExerciseToDetailsDTO)
                .collect(Collectors.toList());

        return NextWorkoutDetailsDTO.builder()
                .id(startedWorkout.getId())
                .workoutId(startedWorkout.getWorkoutId())
                .title(startedWorkout.getTitle())
                .description(startedWorkout.getDescription())
                .workoutExercises(sortedExercises)
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
                .link(exercise.getLink())
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