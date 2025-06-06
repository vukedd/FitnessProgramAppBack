package com.app.fitness.fitnesprogramapp.services.program;

import com.app.fitness.fitnesprogramapp.dtos.program.*;
import com.app.fitness.fitnesprogramapp.dtos.program.details.*;
import com.app.fitness.fitnesprogramapp.dtos.program.startprogram.StartProgramResponseDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.update.*;
import com.app.fitness.fitnesprogramapp.dtos.user.UserProgramDetailsDTO;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.ProgramNotFoundException;
import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.program.*;
import com.app.fitness.fitnesprogramapp.models.set.*;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.models.week.Week;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import com.app.fitness.fitnesprogramapp.repositories.exercise.ExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.exercise.WorkoutExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.metrics.IntensityMetricRepository;
import com.app.fitness.fitnesprogramapp.repositories.metrics.VolumeMetricRepository;
import com.app.fitness.fitnesprogramapp.repositories.program.*;
import com.app.fitness.fitnesprogramapp.repositories.set.SetRepository;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import com.app.fitness.fitnesprogramapp.repositories.week.WeekRepository;
import com.app.fitness.fitnesprogramapp.repositories.workout.WorkoutRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {
    private final ProgramRepository programRepository;
    private final WeekRepository weekRepository;
    private final WorkoutRepository workoutRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final SetRepository setRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final VolumeMetricRepository volumeMetricRepository;
    private final IntensityMetricRepository intensityMetricRepository;
    private final ObjectMapper objectMapper;
    private final StartedProgramRepository startedProgramRepository;

    public Page<ProgramOverviewDTO> getAllProgramsOverview(Pageable pageable) {
        Page<Program> programPage = programRepository.findAll(pageable);
        return programPage.map(ProgramOverviewDTO::fromEntity);
    }

    public Page<ProgramOverviewDTO> searchProgramsByTitle(String title, Pageable pageable) {
        Page<Program> programPage = programRepository.searchByTitle("%"+title+"%", pageable);
        return programPage.map(ProgramOverviewDTO::fromEntity);
    }

    public Page<ProgramOverviewDTO> getStartedProgramsOverview(Pageable pageable, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();

        List<ProgramOverviewDTO> programDTOs = user.getStartedPrograms().stream()
                .map(startedProgram -> {
                    Program program = programRepository.findById(startedProgram.getProgramId()).orElse(null);

                    // Find the latest workout done date for this started program
                    Date latestWorkoutDate = startedProgram.getStartedWeeks().stream()
                            .flatMap(week -> week.getStartedWorkouts().stream())
                            .filter(workout -> workout.getDoneDate() != null) // Only consider completed workouts
                            .map(StartedWorkout::getDoneDate)
                            .max(Date::compareTo)
                            .orElse(new Date(0)); // Use epoch (1970) if no workouts are done

                    return ProgramOverviewDTO.fromEntity(program, startedProgram.getId(), startedProgram.isFinished(), latestWorkoutDate);
                })
                .sorted((dto1, dto2) -> dto2.getLatestWorkoutDate().compareTo(dto1.getLatestWorkoutDate())) // Sort newest first
                .toList();

        return new PageImpl<>(programDTOs, pageable, programDTOs.size());
    }

    public ProgramCreateDTO convertJsonToDTO(String programJson) {
        try {
            return objectMapper.readValue(programJson, ProgramCreateDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ProgramUpdateDTO convertJsonToUpdateDTO(String programJson) {
        try {
            return objectMapper.readValue(programJson, ProgramUpdateDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Program createProgram(ProgramCreateDTO programDTO, MultipartFile image, String username) {
        // Get current user
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new program
        Program program = new Program();
        program.setTitle(programDTO.getName());
        program.setDescription(programDTO.getDescription());
        program.setPublic(programDTO.isPublic());
        program.setCreator(currentUser);
        program.setFollowersNumber(0);

        // Process image if provided
        if (image != null && !image.isEmpty()) {
            try {
                program.setImageData(image.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    public byte[] getProgramImage(Long programId) {
        Program program = programRepository.findById(programId).orElse(null);
        if (program == null) {
            return null;
        }
        return program.getImageData();
    }

    public ProgramDetailsDTO getProgramDetails(Long id, String username) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ProgramNotFoundException("Program not found"));
        return mapProgramToDetailsDTO(program, username);
    }

    private ProgramDetailsDTO mapProgramToDetailsDTO(Program program, String username) {
        ProgramDetailsDTO programDetailsDTO = new ProgramDetailsDTO();
        programDetailsDTO.setId(program.getId());
        programDetailsDTO.setName(program.getTitle());
        programDetailsDTO.setDescription(program.getDescription());
        programDetailsDTO.setPublic(program.isPublic());
        programDetailsDTO.setFollowersNumber(program.getFollowersNumber());
        programDetailsDTO.setRating(program.getRating());
        programDetailsDTO.setCreatedByUser(username.equals(program.getCreator().getUsername()));
        // Map creator
        programDetailsDTO.setCreator(mapUserToProgramDetailsDTO(program.getCreator()));

        // Map weeks with their workouts
        programDetailsDTO.setWeeks(mapWeeksToDetailsDTOs(program.getWeeks()));

        return programDetailsDTO;
    }

    private UserProgramDetailsDTO mapUserToProgramDetailsDTO(User user) {
        UserProgramDetailsDTO creatorDTO = new UserProgramDetailsDTO();
        creatorDTO.setId(user.getId());
        creatorDTO.setUsername(user.getUsername());
        // Add other user fields as needed
        return creatorDTO;
    }

    public List<WeekDetailsDTO> mapWeeksToDetailsDTOs(List<Week> weeks) {
        return weeks.stream()
                .map(this::mapWeekToDetailsDTO)
                .toList();
    }

    public WeekDetailsDTO mapWeekToDetailsDTO(Week week) {
        WeekDetailsDTO weekDetailsDTO = new WeekDetailsDTO();
        weekDetailsDTO.setId(week.getId());
        weekDetailsDTO.setWorkouts(mapWorkoutsToDetailsDTOs(week.getWorkouts()));
        return weekDetailsDTO;
    }

    public List<WorkoutDetailsDTO> mapWorkoutsToDetailsDTOs(List<Workout> workouts) {
        return workouts.stream()
                .sorted(Comparator.comparing(Workout::getPosition))
                .map(this::mapWorkoutToDetailsDTO)
                .toList();
    }

    public WorkoutDetailsDTO mapWorkoutToDetailsDTO(Workout workout) {
        WorkoutDetailsDTO workoutDetailsDTO = new WorkoutDetailsDTO();
        workoutDetailsDTO.setId(workout.getId());
        workoutDetailsDTO.setTitle(workout.getTitle());
        workoutDetailsDTO.setDescription(workout.getDescription());
        workoutDetailsDTO.setWorkoutExercises(mapWorkoutExercisesToDetailsDTOs(workout.getWorkoutExercises()));
        return workoutDetailsDTO;
    }

    public List<WorkoutExerciseDetailsDTO> mapWorkoutExercisesToDetailsDTOs(List<WorkoutExercise> exercises) {
        return exercises.stream()
                .map(this::mapWorkoutExerciseToDetailsDTO)
                .toList();
    }

    public WorkoutExerciseDetailsDTO mapWorkoutExerciseToDetailsDTO(WorkoutExercise exercise) {
        WorkoutExerciseDetailsDTO exerciseDetailsDTO = new WorkoutExerciseDetailsDTO();
        exerciseDetailsDTO.setId(exercise.getId());
        exerciseDetailsDTO.setExercise(mapExerciseToDetailsDTO(exercise.getExercise()));
        exerciseDetailsDTO.setMinimumRestTime(exercise.getMinimumRestTime());
        exerciseDetailsDTO.setMaximumRestTime(exercise.getMaximumRestTime());
        exerciseDetailsDTO.setSets(mapSetsToDetailsDTOs(exercise.getSets()));
        return exerciseDetailsDTO;
    }

    public ExerciseDetailsDTO mapExerciseToDetailsDTO(Exercise exercise) {
        ExerciseDetailsDTO exerciseDetailsDTO = new ExerciseDetailsDTO();
        exerciseDetailsDTO.setId(exercise.getId());
        exerciseDetailsDTO.setTitle(exercise.getTitle());
        exerciseDetailsDTO.setDescription(exercise.getDescription());
        exerciseDetailsDTO.setLink(exercise.getLink());
        return exerciseDetailsDTO;
    }

    public List<SetDetailsDTO> mapSetsToDetailsDTOs(List<Set> sets) {
        return sets.stream()
                .map(this::mapSetToDetailsDTO)
                .toList();
    }

    public SetDetailsDTO mapSetToDetailsDTO(Set set) {
        SetDetailsDTO setDetailsDTO = new SetDetailsDTO();
        setDetailsDTO.setId(set.getId());

        // Map volume
        SetVolumeDetailsDTO volumeDTO = new SetVolumeDetailsDTO(
                set.getVolume().getMinimumVolume(),
                set.getVolume().getMaximumVolume()
        );
        setDetailsDTO.setVolume(volumeDTO);

        // Map intensity
        SetIntensityDetailsDTO intensityDTO = new SetIntensityDetailsDTO(
                set.getIntensity().getMinimumIntensity(),
                set.getIntensity().getMaximumIntensity()
        );
        setDetailsDTO.setIntensity(intensityDTO);

        // Map volume metric
        setDetailsDTO.setVolumeMetric(mapVolumeMetricToDetailsDTO(set.getVolumeMetric()));

        // Map intensity metric
        setDetailsDTO.setIntensityMetric(mapIntensityMetricToDetailsDTO(set.getIntensityMetric()));

        return setDetailsDTO;
    }

    public VolumeMetricDetailsDTO mapVolumeMetricToDetailsDTO(VolumeMetric volumeMetric) {
        VolumeMetricDetailsDTO volumeMetricDTO = new VolumeMetricDetailsDTO();
        volumeMetricDTO.setId(volumeMetric.getId());
        volumeMetricDTO.setRange(volumeMetric.isRange());
        volumeMetricDTO.setTitle(volumeMetric.getTitle());
        volumeMetricDTO.setMetricSymbol(volumeMetric.getMetricSymbol());
        return volumeMetricDTO;
    }

    public IntensityMetricDetailsDTO mapIntensityMetricToDetailsDTO(IntensityMetric intensityMetric) {
        IntensityMetricDetailsDTO intensityMetricDTO = new IntensityMetricDetailsDTO();
        intensityMetricDTO.setId(intensityMetric.getId());
        intensityMetricDTO.setMinimumIntensity(intensityMetric.getMinimumIntensity());
        intensityMetricDTO.setMaximumIntensity(intensityMetric.getMaximumIntensity());
        intensityMetricDTO.setRange(intensityMetric.isRange());
        intensityMetricDTO.setTitle(intensityMetric.getTitle());
        intensityMetricDTO.setMetricSymbol(intensityMetric.getMetricSymbol());
        return intensityMetricDTO;
    }

    private Week createWeek(WeekDTO weekDTO) {
        Week week = new Week();
        List<Workout> workouts = new ArrayList<>();

        int i = 0;
        for (WorkoutDTO workoutDTO : weekDTO.getWorkouts()) {
            Workout workout = createWorkout(workoutDTO, i);
            workouts.add(workout);
            i++;
        }

        week.setWorkouts(workouts);
        return weekRepository.save(week);
    }

    private Workout createWorkout(WorkoutDTO workoutDTO, int position) {
        Workout workout = new Workout();
        workout.setTitle(workoutDTO.getTitle());
        workout.setDescription(workoutDTO.getDescription());
        workout.setPosition(position);

        List<WorkoutExercise> workoutExercises = new ArrayList<>();
        int i = 0;
        for (WorkoutExerciseDTO exerciseDTO : workoutDTO.getWorkoutExercises()) {
            WorkoutExercise workoutExercise = createWorkoutExercise(exerciseDTO, i);
            workoutExercises.add(workoutExercise);
            i++;
        }

        workout.setWorkoutExercises(workoutExercises);
        return workoutRepository.save(workout);
    }

    private WorkoutExercise createWorkoutExercise(WorkoutExerciseDTO exerciseDTO, int position) {
        WorkoutExercise workoutExercise = new WorkoutExercise();

        // Get exercise by ID
        Exercise exercise = exerciseRepository.findById(exerciseDTO.getExercise())
                .orElseThrow(() -> new RuntimeException("Exercise not found with ID: " + exerciseDTO.getExercise()));

        workoutExercise.setExercise(exercise);
        workoutExercise.setMinimumRestTime(exerciseDTO.getMinimumRestTime());
        workoutExercise.setMaximumRestTime(exerciseDTO.getMaximumRestTime());
        workoutExercise.setPosition(position);

        // Create sets
        List<Set> sets = new ArrayList<>();
        for (CreateSetDTO setDTO : exerciseDTO.getSets()) {
            Set set = createWorkoutExerciseSet(
                    setDTO
            );
            sets.add(set);
        }

        workoutExercise.setSets(sets);
        return workoutExerciseRepository.save(workoutExercise);
    }

    public Set createWorkoutExerciseSet(
            CreateSetDTO setDTO) {

        Set set = new Set();

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

        return setRepository.save(set);
    }


    public StartProgramResponseDTO startProgram(Long programId, String username) {
        Program program = programRepository.findById(programId).orElseThrow(() -> new RuntimeException("Program not found!"));
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));
        StartedProgram startedProgram = new StartedProgram();
        startedProgram.setProgramId(program.getId());
        startedProgram.setStartDate(new Date());
        startedProgramRepository.save(startedProgram);
        user.getStartedPrograms().add(startedProgram);
        userRepository.save(user);
        return new StartProgramResponseDTO(startedProgram.getId(), program.getTitle());

    }


    @Transactional
    public Program updateProgram(Long programId, ProgramUpdateDTO programDTO, MultipartFile image, String username) {
        // Get current user
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the existing program
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        // Check if the current user is the creator of the program
        if (!program.getCreator().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to edit this program");
        }

        // Update program details
        program.setTitle(programDTO.getName());
        program.setPublic(programDTO.isPublic());
        program.setDescription(programDTO.getDescription());

        // Process image if provided
        if (image != null && !image.isEmpty()) {
            try {
                program.setImageData(image.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            program.setImageData(null);
        }

        // Update weeks using patch approach
        updateProgramWeeks(program, programDTO.getWeeks());

        return programRepository.save(program);
    }

    private void updateProgramWeeks(Program program, List<UpdateWeekDTO> weekDTOs) {
        List<Week> existingWeeks = new ArrayList<>(program.getWeeks());
        List<Week> updatedWeeks = new ArrayList<>();

        // Process all weeks from the DTO
        for (UpdateWeekDTO weekDTO : weekDTOs) {
            // Try to find existing week by ID
            Week week = findOrCreateWeek(existingWeeks, weekDTO);

            // Update the week's workouts
            updateWeekWorkouts(week, weekDTO.getWorkouts());

            updatedWeeks.add(week);
        }

        // Clear the list first to avoid orphan deletion issues
        program.getWeeks().clear();

        // Add all updated weeks
        program.getWeeks().addAll(updatedWeeks);

        // Now remove weeks that are no longer needed
        List<Week> weeksToRemove = existingWeeks.stream()
                .filter(week -> updatedWeeks.stream()
                        .noneMatch(updatedWeek -> updatedWeek.getId().equals(week.getId())))
                .toList();

        weekRepository.deleteAll(weeksToRemove);
    }

    private Week findOrCreateWeek(List<Week> existingWeeks, UpdateWeekDTO weekDTO) {
        // If ID is provided and exists, update the existing week
        if (weekDTO.getId() != null) {
            for (Week existingWeek : existingWeeks) {
                if (existingWeek.getId().equals(weekDTO.getId())) {
                    return existingWeek;
                }
            }
        }

        // If not found or ID is null, create a new week
        Week newWeek = new Week();
        newWeek.setWorkouts(new ArrayList<>());
        return weekRepository.save(newWeek);
    }

    private void updateWeekWorkouts(Week week, List<UpdateWorkoutDTO> workoutDTOs) {
        List<Workout> existingWorkouts = new ArrayList<>(week.getWorkouts());
        List<Workout> updatedWorkouts = new ArrayList<>();

        // Process all workouts from the DTO
        int i = 0;
        for (UpdateWorkoutDTO workoutDTO : workoutDTOs) {
            // Try to find existing workout by ID
            Workout workout = findOrCreateWorkout(existingWorkouts, workoutDTO);

            // Update workout details
            workout.setTitle(workoutDTO.getTitle());
            workout.setDescription(workoutDTO.getDescription());
            workout.setPosition(i);

            // Replace all workout exercises with new ones
            replaceWorkoutExercises(workout, workoutDTO.getWorkoutExercises());

            updatedWorkouts.add(workoutRepository.save(workout));
            i++;
        }

        // Clear the list first to avoid orphan deletion issues
        week.getWorkouts().clear();

        // Add all updated workouts
        week.getWorkouts().addAll(updatedWorkouts);

        // Now remove workouts that are no longer needed
        List<Workout> workoutsToRemove = existingWorkouts.stream()
                .filter(workout -> updatedWorkouts.stream()
                        .noneMatch(updatedWorkout -> updatedWorkout.getId().equals(workout.getId())))
                .toList();

        workoutRepository.deleteAll(workoutsToRemove);
    }

    private Workout findOrCreateWorkout(List<Workout> existingWorkouts, UpdateWorkoutDTO workoutDTO) {
        // If ID is provided and exists, update the existing workout
        if (workoutDTO.getId() != null) {
            for (Workout existingWorkout : existingWorkouts) {
                if (existingWorkout.getId().equals(workoutDTO.getId())) {
                    return existingWorkout;
                }
            }
        }

        // If not found or ID is 0, create a new workout
        Workout newWorkout = new Workout();
        newWorkout.setWorkoutExercises(new ArrayList<>());
        return newWorkout;
    }

    private void replaceWorkoutExercises(Workout workout, List<UpdateWorkoutExerciseDTO> exerciseDTOs) {
        // Create a copy of existing exercises
        List<WorkoutExercise> existingExercises = new ArrayList<>(workout.getWorkoutExercises());
        List<WorkoutExercise> newExercises = new ArrayList<>();

        // Create new workout exercises
        int i = 0;
        for (UpdateWorkoutExerciseDTO exerciseDTO : exerciseDTOs) {
            WorkoutExercise workoutExercise = createNewWorkoutExercise(exerciseDTO, i);
            newExercises.add(workoutExercise);
            i++;
        }

        // Clear the list first to avoid orphan deletion issues
        workout.getWorkoutExercises().clear();

        // Add all new exercises
        workout.getWorkoutExercises().addAll(newExercises);

        // Delete all existing workout exercises and their sets that are no longer needed
        for (WorkoutExercise exercise : existingExercises) {
            setRepository.deleteAll(exercise.getSets());
        }
        workoutExerciseRepository.deleteAll(existingExercises);
    }

    private WorkoutExercise createNewWorkoutExercise(UpdateWorkoutExerciseDTO exerciseDTO, int position) {
        WorkoutExercise workoutExercise = new WorkoutExercise();

        // Get referenced exercise
        Exercise exercise = exerciseRepository.findById(exerciseDTO.getExercise())
                .orElseThrow(() -> new RuntimeException("Exercise not found with ID: " + exerciseDTO.getExercise()));

        workoutExercise.setExercise(exercise);
        workoutExercise.setMinimumRestTime(exerciseDTO.getMinimumRestTime());
        workoutExercise.setMaximumRestTime(exerciseDTO.getMaximumRestTime());
        workoutExercise.setPosition(position);

        // Create new sets
        List<Set> sets = new ArrayList<>();
        for (UpdateWorkoutExerciseSetDTO setDTO : exerciseDTO.getSets()) {
            Set set = createNewSet(setDTO);
            sets.add(set);
        }
        workoutExercise.setSets(sets);

        return workoutExerciseRepository.save(workoutExercise);
    }

    private Set createNewSet(UpdateWorkoutExerciseSetDTO setDTO) {
        Set set = new Set();

        // Get metrics by ID
        VolumeMetric volumeMetric = volumeMetricRepository.findById(setDTO.getVolumeMetric())
                .orElseThrow(() -> new RuntimeException("Volume metric not found with ID: " + setDTO.getVolumeMetric()));

        IntensityMetric intensityMetric = intensityMetricRepository.findById(setDTO.getIntensityMetric())
                .orElseThrow(() -> new RuntimeException("Intensity metric not found with ID: " + setDTO.getIntensityMetric()));

        set.setVolumeMetric(volumeMetric);
        set.setIntensityMetric(intensityMetric);

        // Create volume
        SetVolume volume = new SetVolume();
        volume.setMinimumVolume(setDTO.getVolumeMin());
        volume.setMaximumVolume(setDTO.getVolumeMax());
        set.setVolume(volume);

        // Create intensity
        SetIntensity intensity = new SetIntensity();
        intensity.setMinimumIntensity(setDTO.getIntensityMin());
        intensity.setMaximumIntensity(setDTO.getIntensityMax());
        set.setIntensity(intensity);

        return setRepository.save(set);
    }

    public void deleteProgram(Long programId, String username) {
        // Find the program
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found with id: " + programId));

        // Check if the user is the owner of the program
        if (!program.getCreator().getUsername().equals(username)) {
            throw new RuntimeException("You don't have permission to delete this program");
        }

        // Delete the program
        programRepository.delete(program);
    }

    public Page<ProgramOverviewDTO> getProgramsCreatedByMe(String userName, String title, Pageable pageable) {
        Page<Program> programsCreatedByMe;

        if (title == null || title.trim().isEmpty()) {
            programsCreatedByMe = programRepository.findByCreatorUsername(userName, pageable);
        } else {
            programsCreatedByMe = programRepository.findByCreatorUsernameAndTitleContainingIgnoreCase(
                    userName, title.trim(), pageable);
        }

        return programsCreatedByMe.map(ProgramOverviewDTO::fromEntity);
    }

}