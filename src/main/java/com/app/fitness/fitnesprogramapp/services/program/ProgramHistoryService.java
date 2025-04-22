package com.app.fitness.fitnesprogramapp.services.program;

import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.DoneSetsHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.ProgramHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.StartedWeekHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.StartedWorkoutHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.WorkoutExerciseHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.volume.DailyVolumeReportDTO;
import com.app.fitness.fitnesprogramapp.dtos.volume.DailyWorkoutDurationReportDTO;
import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.program.StartedProgram;
import com.app.fitness.fitnesprogramapp.models.set.DoneSet;
import com.app.fitness.fitnesprogramapp.models.set.Set;
import com.app.fitness.fitnesprogramapp.models.week.StartedWeek;
import com.app.fitness.fitnesprogramapp.models.week.Week;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import com.app.fitness.fitnesprogramapp.repositories.program.StartedProgramRepository;
import com.app.fitness.fitnesprogramapp.repositories.set.DoneSetRepository;
import com.app.fitness.fitnesprogramapp.repositories.week.WeekRepository;
import com.app.fitness.fitnesprogramapp.repositories.workout.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramHistoryService {
    private final StartedProgramRepository startedProgramRepository;
    private final ProgramService programService;
    private final DoneSetRepository doneSetRepository;
    private final WorkoutRepository workoutRepository;
    private final WeekRepository weekRepository;

    public ProgramHistoryDTO getProgramHistory(Long startedProgramId, String username) {
        ProgramHistoryDTO programHistoryDTO = new ProgramHistoryDTO();
        StartedProgram startedProgram= startedProgramRepository.findById(startedProgramId).orElseThrow();
        programHistoryDTO.setStartedProgramId(startedProgram.getId());
        programHistoryDTO.setProgramId(startedProgram.getProgram().getId());
        programHistoryDTO.setTitle(startedProgram.getProgram().getTitle());
        programHistoryDTO.setStartedWeeks(new ArrayList<>());
        programHistoryDTO.setWeeks(new ArrayList<>());

        List<StartedWeek> startedWeeks=startedProgram.getStartedWeeks();
        java.util.Set<Long> startedWeekSet = startedWeeks.stream().map(StartedWeek::getId).collect(Collectors.toSet());
        List<Week> allWeeks=startedProgram.getProgram().getWeeks();

        for(StartedWeek startedWeek:startedWeeks){
            programHistoryDTO.getStartedWeeks().add(mapStartedWeekToStartedWeekHistoryDTO(startedWeek));
        }

        for(Week week:allWeeks){
            if(!startedWeekSet.contains(week.getId())){
                programHistoryDTO.getWeeks().add(programService.mapWeekToDetailsDTO(week));
            }
        }

        return programHistoryDTO;
    }

    private StartedWeekHistoryDTO mapStartedWeekToStartedWeekHistoryDTO(StartedWeek startedWeek) {
        StartedWeekHistoryDTO startedWeekHistoryDTO = new StartedWeekHistoryDTO();
        startedWeekHistoryDTO.setId(startedWeek.getId());
        startedWeekHistoryDTO.setWeekId(startedWeek.getWeekId());
        startedWeekHistoryDTO.setStartDate(startedWeek.getStartDate());
        startedWeekHistoryDTO.setFinished(startedWeek.isFinished());
        startedWeekHistoryDTO.setDoneDate(startedWeek.getDoneDate());

        startedWeekHistoryDTO.setStartedWorkouts(new ArrayList<>());
        startedWeekHistoryDTO.setWorkouts(new ArrayList<>());

        List<StartedWorkout> startedWorkouts = startedWeek.getStartedWorkouts();
        java.util.Set<Long> startedWorkoutSet = startedWorkouts.stream().map(StartedWorkout::getWorkoutId).collect(Collectors.toSet());
        Optional<Week> originalWeek = weekRepository.findById(startedWeek.getWeekId());
        List<Workout> allWorkouts = new ArrayList<>();
        if (originalWeek.isPresent()) {
            allWorkouts = originalWeek.get().getWorkouts();
        }

        // Add all started workouts
        for (StartedWorkout startedWorkout : startedWorkouts) {
            startedWeekHistoryDTO.getStartedWorkouts().add(mapStartedWorkoutToHistoryDTO(startedWorkout));
        }

        // Add remaining workouts that haven't been started yet
        for(Workout workout:allWorkouts){
            if(!startedWorkoutSet.contains(workout.getId())){
                startedWeekHistoryDTO.getWorkouts().add(programService.mapWorkoutToDetailsDTO(workout));
            }
        }

        return startedWeekHistoryDTO;
    }

    private StartedWorkoutHistoryDTO mapStartedWorkoutToHistoryDTO(StartedWorkout startedWorkout) {
        StartedWorkoutHistoryDTO dto = new StartedWorkoutHistoryDTO();
        dto.setId(startedWorkout.getId());
        dto.setTitle(startedWorkout.getTitle());
        dto.setDescription(startedWorkout.getDescription());
        dto.setNumber(startedWorkout.getNumber());
        dto.setFinished(startedWorkout.isFinished());

        dto.setWorkoutExercises(new ArrayList<>());

        // Map workout exercises with their sets
        for (WorkoutExercise exercise : startedWorkout.getWorkoutExercises()) {
            WorkoutExerciseHistoryDTO exerciseDTO = new WorkoutExerciseHistoryDTO();
            exerciseDTO.setId(exercise.getId());
            exerciseDTO.setTitle(exercise.getExercise().getTitle());
            exerciseDTO.setSets(new ArrayList<>());
            exerciseDTO.setDoneSets(new ArrayList<>());

            // Find and map done sets for this exercise
            List<DoneSet> doneSetsForExercise = startedWorkout.getDoneSets().stream()
                    .filter(doneSet -> doneSet.getWorkoutExercise().getId().equals(exercise.getId()))
                    .collect(Collectors.toList());

            // Map done sets
            for (DoneSet doneSet : doneSetsForExercise) {
                exerciseDTO.getDoneSets().add(mapDoneSetToHistoryDTO(doneSet));
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

    public DoneSetsHistoryDTO mapDoneSetToHistoryDTO(DoneSet doneSet){
        DoneSetsHistoryDTO doneSetDTO = new DoneSetsHistoryDTO();
        doneSetDTO.setId(doneSet.getId());
        doneSetDTO.setSetDetails(programService.mapSetToDetailsDTO(doneSet.getSet()));
        doneSetDTO.setVolume(doneSet.getVolume());
        doneSetDTO.setIntensity(doneSet.getIntensity());
        doneSetDTO.setDate(doneSet.getDate());
        doneSetDTO.setWeightLifted(doneSet.getWeightLifted());
        return doneSetDTO;
    }

    public List<DailyVolumeReportDTO> getWeeklyVolumeReport(String username, LocalDate startDate, LocalDate endDate) {
        List<Object[]> weeklyVolumeRaw = doneSetRepository.fetchDailyVolumeByUserForDateRange(username, startDate, endDate.plusDays(1));
        List<DailyVolumeReportDTO> weeklyVolumeReport = new ArrayList<>();

        LocalDate lastDate = endDate;
        int rawIndex = 0;

        while (weeklyVolumeReport.size() < 7) {
            if (rawIndex < weeklyVolumeRaw.size() && lastDate.equals(((Date) weeklyVolumeRaw.get(rawIndex)[0]).toLocalDate())) {
                weeklyVolumeReport.add(new DailyVolumeReportDTO(lastDate, (Long) weeklyVolumeRaw.get(rawIndex)[1]));
                rawIndex++;
            } else {
                weeklyVolumeReport.add(new DailyVolumeReportDTO(lastDate, 0L));
            }

            if (weeklyVolumeReport.size() == 7) break;

            lastDate = lastDate.minusDays(1);
        }

        return weeklyVolumeReport;
    }

    public List<DailyWorkoutDurationReportDTO> getWeeklyWorkoutDurationReport(String username, LocalDate startDate, LocalDate endDate) {
        List<Object[]> weeklyWorkoutDurationRaw = workoutRepository.fetchWeeklyWorkoutDurationReport(username, startDate.toString(), endDate.toString());
        List<DailyWorkoutDurationReportDTO> weeklyWorkoutDurationReport = new ArrayList<>();

        LocalDate lastDate = endDate;
        int rawIndex = 0;

        while (weeklyWorkoutDurationReport.size() < 7) {
            if (rawIndex < weeklyWorkoutDurationRaw.size() && lastDate.equals(((Date) weeklyWorkoutDurationRaw.get(rawIndex)[0]).toLocalDate())) {
                weeklyWorkoutDurationReport.add(new DailyWorkoutDurationReportDTO(((BigDecimal) weeklyWorkoutDurationRaw.get(rawIndex)[1]).floatValue(), lastDate));
                rawIndex++;
            } else {
                weeklyWorkoutDurationReport.add(new DailyWorkoutDurationReportDTO(0.0F, lastDate));
            }

            if (weeklyWorkoutDurationReport.size() == 7) break;

            lastDate = lastDate.minusDays(1);
        }

        return weeklyWorkoutDurationReport;
    }
}