package com.app.fitness.fitnesprogramapp.services.program;

import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.DoneSetsHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.ProgramHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.StartedWeekHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.StartedWorkoutHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.WorkoutExerciseHistoryDTO;
import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.program.StartedProgram;
import com.app.fitness.fitnesprogramapp.models.set.DoneSet;
import com.app.fitness.fitnesprogramapp.models.set.Set;
import com.app.fitness.fitnesprogramapp.models.week.StartedWeek;
import com.app.fitness.fitnesprogramapp.models.week.Week;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import com.app.fitness.fitnesprogramapp.repositories.program.StartedProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramHistoryService {
    private final StartedProgramRepository startedProgramRepository;
    private final ProgramService programService;

    public ProgramHistoryDTO getProgramHistory(Long startedProgramId, String username) {
        ProgramHistoryDTO programHistoryDTO = new ProgramHistoryDTO();
        StartedProgram startedProgram= startedProgramRepository.findById(startedProgramId).orElseThrow();
        programHistoryDTO.setStartedProgramId(startedProgram.getId());
        programHistoryDTO.setProgramId(startedProgram.getProgram().getId());
        programHistoryDTO.setTitle(startedProgram.getProgram().getTitle());
        programHistoryDTO.setStartedWeeks(new ArrayList<>());
        programHistoryDTO.setWeeks(new ArrayList<>());
        int nextWeekIndex = 0;
        List<StartedWeek> startedWeeks=startedProgram.getStartedWeeks();
        List<Week> allWeeks=startedProgram.getProgram().getWeeks();
        for(StartedWeek startedWeek:startedWeeks){
            nextWeekIndex++;
            programHistoryDTO.getStartedWeeks().add(mapStartedWeekToStartedWeekHistoryDTO(startedWeek));
        }
        for(int i=nextWeekIndex; i<allWeeks.size(); i++){
            Week week=allWeeks.get(i);
            programHistoryDTO.getWeeks().add(programService.mapWeekToDetailsDTO(week));
        }

        return programHistoryDTO;
    }

    private StartedWeekHistoryDTO mapStartedWeekToStartedWeekHistoryDTO(StartedWeek startedWeek) {
        StartedWeekHistoryDTO startedWeekHistoryDTO = new StartedWeekHistoryDTO();
        startedWeekHistoryDTO.setId(startedWeek.getId());
        startedWeekHistoryDTO.setWeekId(startedWeek.getWeek().getId());
        startedWeekHistoryDTO.setStartDate(startedWeek.getStartDate());
        startedWeekHistoryDTO.setFinished(startedWeek.isFinished());
        startedWeekHistoryDTO.setDoneDate(startedWeek.getDoneDate());

        startedWeekHistoryDTO.setStartedWorkouts(new ArrayList<>());
        startedWeekHistoryDTO.setWorkouts(new ArrayList<>());

        int nextWorkoutIndex = 0;
        List<StartedWorkout> startedWorkouts = startedWeek.getStartedWorkouts();
        List<Workout> allWorkouts = startedWeek.getWeek().getWorkouts();

        // Add all started workouts
        for (StartedWorkout startedWorkout : startedWorkouts) {
            nextWorkoutIndex++;
            startedWeekHistoryDTO.getStartedWorkouts().add(mapStartedWorkoutToHistoryDTO(startedWorkout));
        }

        // Add remaining workouts that haven't been started yet
        for (int i = nextWorkoutIndex; i < allWorkouts.size(); i++) {
            Workout workout = allWorkouts.get(i);
            startedWeekHistoryDTO.getWorkouts().add(programService.mapWorkoutToDetailsDTO(workout));
        }

        return startedWeekHistoryDTO;
    }

    private StartedWorkoutHistoryDTO mapStartedWorkoutToHistoryDTO(StartedWorkout startedWorkout) {
        StartedWorkoutHistoryDTO dto = new StartedWorkoutHistoryDTO();
        dto.setId(startedWorkout.getId());
        dto.setTitle(startedWorkout.getWorkout().getTitle());
        dto.setDescription(startedWorkout.getWorkout().getDescription());
        dto.setNumber(startedWorkout.getWorkout().getNumber());

        dto.setWorkoutExercises(new ArrayList<>());

        // Map workout exercises with their sets
        for (WorkoutExercise exercise : startedWorkout.getWorkout().getWorkoutExercises()) {
            WorkoutExerciseHistoryDTO exerciseDTO = new WorkoutExerciseHistoryDTO();
            exerciseDTO.setId(exercise.getId());
            exerciseDTO.setSets(new ArrayList<>());
            exerciseDTO.setDoneSets(new ArrayList<>());

            // Find and map done sets for this exercise
            List<DoneSet> doneSetsForExercise = startedWorkout.getDoneSets().stream()
                    .filter(doneSet -> doneSet.getWorkoutExercise().getId().equals(exercise.getId()))
                    .collect(Collectors.toList());

            // Map done sets
            for (DoneSet doneSet : doneSetsForExercise) {
                DoneSetsHistoryDTO doneSetDTO = new DoneSetsHistoryDTO();
                doneSetDTO.setId(doneSet.getId());
                doneSetDTO.setSetDetails(programService.mapSetToDetailsDTO(doneSet.getSet()));
                doneSetDTO.setVolume(doneSet.getVolume());
                doneSetDTO.setIntensity(doneSet.getIntensity());
                doneSetDTO.setDate(doneSet.getDate());
                doneSetDTO.setWeightLifted(doneSet.getWeightLifted());
                exerciseDTO.getDoneSets().add(doneSetDTO);
            }

            // Get all sets for this exercise
            List<Set> allSets = exercise.getSets();

            // Track which sets have been done already
            List<Long> doneSetIds = doneSetsForExercise.stream()
                    .map(doneSet -> doneSet.getSet().getId())
                    .collect(Collectors.toList());

            // Add remaining sets that haven't been done yet
            for (Set set : allSets) {
                if (!doneSetIds.contains(set.getId())) {
                    exerciseDTO.getSets().add(programService.mapSetToDetailsDTO(set));
                }
            }

            dto.getWorkoutExercises().add(exerciseDTO);
        }

        return dto;
    }
}