package com.app.fitness.fitnesprogramapp.services.workout;

import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateDoneSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutDTO;
import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.program.Program;
import com.app.fitness.fitnesprogramapp.models.program.StartedProgram;
import com.app.fitness.fitnesprogramapp.models.set.DoneSet;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    private final StartedWeekRepository startedWeekRepository;
    private final StartedProgramRepository startedProgramRepository;
    private final StartedWorkoutRepository startedWorkoutRepository;
    private final DoneSetRepository doneSetRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final SetRepository setRepository;

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
        if (nextWorkoutDTO.getStartedWorkout() != null && "start".equals(nextWorkoutDTO.getAction())) {
            Workout workoutToStart = nextWorkoutDTO.getStartedWorkout().getWorkout();

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
            nextWorkoutDTO.setStartedWorkout(savedWorkout);
        }

        return nextWorkoutDTO;
    }

    /**
     * Processes a complete set request
     * @param workoutId The started workout ID
     * @param doneSetDTO The completed set data
     * @return Updated StartedWorkout or null if not found
     */
    public StartedWorkout processCompleteSet(Long workoutId, CreateDoneSetDTO doneSetDTO) {
        Optional<StartedWorkout> startedWorkoutOpt = startedWorkoutRepository.findById(workoutId);

        if (startedWorkoutOpt.isEmpty()) {
            return null;
        }

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

        StartedWorkout startedWorkout = startedWorkoutOpt.get();

        // Add the done set to the workout
        startedWorkout.getDoneSets().add(doneSet);

        // Find the started week and program for this workout
        StartedProgram startedProgram = findStartedProgramForWorkout(startedWorkout);
        if (startedProgram == null) {
            throw new IllegalStateException("Could not find program for this workout");
        }

        // Find the started week for this workout
        Optional<StartedWeek> startedWeek =
                startedProgram.getStartedWeeks().stream()
                        .filter(week -> week.getStartedWorkouts().stream()
                                .anyMatch(workout -> workout.getId().equals(startedWorkout.getId())))
                        .findFirst();

        if (startedWeek.isEmpty()) {
            throw new IllegalStateException("Could not find week for this workout");
        }

        // Update workout status based on set completion
        return completeSet(
                startedWorkout,
                startedWeek.get(),
                startedProgram
        );
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
                    .startedWorkout(tempStartedWorkout)
                    .action("start")
                    .build();
        }

        // Sort workouts by ID to get the latest one
        startedWorkouts.sort(Comparator.comparing(StartedWorkout::getId));
        StartedWorkout latestWorkout = startedWorkouts.getLast();

        if (!latestWorkout.isFinished()) {
            // Latest workout is not finished, continue it
            return NextWorkoutDTO.builder()
                    .startedWorkout(latestWorkout)
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
                    .startedWorkout(tempStartedWorkout)
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
                .startedWorkout(tempStartedWorkout)
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
                    .startedWorkout(tempStartedWorkout)
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

    /**
     * Completes a set and updates the workout and week status
     * @param startedWorkout The workout containing the completed set
     * @param startedWeek The week containing the workout
     * @param startedProgram The program containing the week
     * @return Updated started workout
     */
    public StartedWorkout completeSet(StartedWorkout startedWorkout, StartedWeek startedWeek, StartedProgram startedProgram) {
        // Check if all sets for this workout have been completed
        boolean allSetsCompleted = checkAllSetsCompleted(startedWorkout);

        if (allSetsCompleted) {
            // Mark workout as finished
            startedWorkout.setFinished(true);
            startedWorkout.setDoneDate(new Date());
            startedWorkoutRepository.save(startedWorkout);

            // Check if this was the last workout in the week
            checkWeekCompletion(startedWeek);

            // Check if this was the last week in the program
            if (startedWeek.isFinished()) {
                checkProgramCompletion(startedProgram);
            }
        }

        return startedWorkout;
    }

    /**
     * Checks if all sets in a workout have been completed
     * @param startedWorkout The workout to check
     * @return true if all sets are completed, false otherwise
     */
    private boolean checkAllSetsCompleted(StartedWorkout startedWorkout) {
        Workout workout = startedWorkout.getWorkout();
        int totalSetCount = 0;

        // Count total sets in the workout
        for (var exercise : workout.getWorkoutExercises()) {
            totalSetCount += exercise.getSets().size();
        }

        // Check if all sets have been completed
        return startedWorkout.getDoneSets().size() >= totalSetCount;
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
     * Finds the started program containing a specific started workout
     * @param startedWorkout The workout to find
     * @return The containing started program or null if not found
     */
    private StartedProgram findStartedProgramForWorkout(StartedWorkout startedWorkout) {
        // This could be more efficient with proper database queries
        Iterable<StartedProgram> allPrograms = startedProgramRepository.findAll();

        for (StartedProgram program : allPrograms) {
            for (StartedWeek week : program.getStartedWeeks()) {
                if (week.getStartedWorkouts().stream()
                        .anyMatch(workout -> workout.getId().equals(startedWorkout.getId()))) {
                    return program;
                }
            }
        }

        return null;
    }
}