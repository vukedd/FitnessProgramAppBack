package com.app.fitness.fitnesprogramapp.services.program;

import com.app.fitness.fitnesprogramapp.dtos.program.*;
import com.app.fitness.fitnesprogramapp.dtos.program.currentworkout.CurrentWorkoutDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.currentworkout.CurrentWorkoutExerciseDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.currentworkout.CurrentSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.details.*;
import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateDoneSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateExerciseHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateExerciseHistoryResponseDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.startprogram.StartProgramResponseDTO;
import com.app.fitness.fitnesprogramapp.dtos.user.UserProgramDetailsDTO;
import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.program.*;
import com.app.fitness.fitnesprogramapp.models.set.*;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.models.week.Week;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import com.app.fitness.fitnesprogramapp.repositories.exercise.ExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.exercise.WorkoutExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.metrics.IntensityMetricRepository;
import com.app.fitness.fitnesprogramapp.repositories.metrics.VolumeMetricRepository;
import com.app.fitness.fitnesprogramapp.repositories.program.*;
import com.app.fitness.fitnesprogramapp.repositories.set.DoneSetRepository;
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
import java.util.Date;
import java.util.List;

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
    private final DoneSetRepository doneSetRepository;
    private final StartedProgramRepository startedProgramRepository;

    public Page<ProgramOverviewDTO> getAllProgramsOverview(Pageable pageable) {
        Page<Program> programPage = programRepository.findAll(pageable);
        return programPage.map(ProgramOverviewDTO::fromEntity);
    }

    public Page<ProgramOverviewDTO> searchProgramsByTitle(String title, Pageable pageable) {
        String searchTerm = "%" + title + "%";
        Page<Program> programPage = programRepository.searchByTitle(searchTerm, pageable);
        return programPage.map(ProgramOverviewDTO::fromEntity);
    }

    public Page<ProgramOverviewDTO> getStartedProgramsOverview(Pageable pageable, String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<ProgramOverviewDTO> programDTOs = user.getStartedPrograms().stream()
                .map(startedProgram -> {
                    Program program = startedProgram.getProgram();
                    return ProgramOverviewDTO.fromEntity(program, startedProgram.getId());
                })
                .toList();

        return new PageImpl<>(programDTOs, pageable, programDTOs.size());
    }

    public ProgramCreateDTO convertJsonToDTO(String programJson)  {
        try {
            return objectMapper.readValue(programJson, ProgramCreateDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public Program createProgram(ProgramCreateDTO programDTO, MultipartFile image, String username)  {
        // Get current user
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new program
        Program program = new Program();
        program.setTitle(programDTO.getName());
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
        Program program=programRepository.findById(programId).orElseThrow(() -> new RuntimeException("Program not found"));
        return program.getImageData();
    }

    public ProgramDetailsDTO getProgramDetails(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found"));
        return mapProgramToDetailsDTO(program);
    }

    private ProgramDetailsDTO mapProgramToDetailsDTO(Program program) {
        ProgramDetailsDTO programDetailsDTO = new ProgramDetailsDTO();
        programDetailsDTO.setId(program.getId());
        programDetailsDTO.setName(program.getTitle());
        programDetailsDTO.setFollowersNumber(program.getFollowersNumber());
        programDetailsDTO.setRating(program.getRating());

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
                .map(this::mapWorkoutToDetailsDTO)
                .toList();
    }

    public WorkoutDetailsDTO mapWorkoutToDetailsDTO(Workout workout) {
        WorkoutDetailsDTO workoutDetailsDTO = new WorkoutDetailsDTO();
        workoutDetailsDTO.setId(workout.getId());
        workoutDetailsDTO.setTitle(workout.getTitle());
        workoutDetailsDTO.setDescription(workout.getDescription());
        workoutDetailsDTO.setNumber(workout.getNumber());
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
        exerciseDetailsDTO.setWorkoutExerciseSets(mapSetsToDetailsDTOs(exercise.getSets()));
        return exerciseDetailsDTO;
    }

    public ExerciseDetailsDTO mapExerciseToDetailsDTO(Exercise exercise) {
        ExerciseDetailsDTO exerciseDetailsDTO = new ExerciseDetailsDTO();
        exerciseDetailsDTO.setId(exercise.getId());
        exerciseDetailsDTO.setTitle(exercise.getTitle());
        exerciseDetailsDTO.setDescription(exercise.getDescription());
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
        workoutExercise.setMinimumRestTime(exerciseDTO.getMinimumRestTime());
        workoutExercise.setMaximumRestTime(exerciseDTO.getMaximumRestTime());

        // Create sets
        List<Set> sets = new ArrayList<>();
        for (WorkoutExerciseSetDTO setDTO : exerciseDTO.getSets()) {
            Set set = createWorkoutExerciseSet(
                    setDTO
            );
            sets.add(set);
        }

        workoutExercise.setSets(sets);
        return workoutExerciseRepository.save(workoutExercise);
    }

    private Set createWorkoutExerciseSet(
            WorkoutExerciseSetDTO setDTO) {

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

    public CreateExerciseHistoryResponseDTO createExerciseHistory(String username,CreateExerciseHistoryDTO historyDTO) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        for(CreateDoneSetDTO setDTO:historyDTO.getDoneSets()){
            WorkoutExercise workoutExercise=workoutExerciseRepository.findById(setDTO.getWorkoutExerciseId()).orElseThrow(()->new RuntimeException("Workout exercise not found!"));
            Set set = setRepository.findById(setDTO.getSetId()).orElseThrow(()->new RuntimeException("Workout exercise set not found!"));
            DoneSet doneSet=new DoneSet();
            doneSet.setWorkoutExercise(workoutExercise);
            doneSet.setSet(set);
            doneSet.setVolume(setDTO.getVolume());
            doneSet.setIntensity(setDTO.getIntensity());
            doneSetRepository.save(doneSet);
        }
        return new CreateExerciseHistoryResponseDTO("Sets added to history");

    }

    public CurrentWorkoutDTO getCurrentWorkout(Long workoutId) {
        Workout workout=workoutRepository.findById(workoutId).orElseThrow(() -> new RuntimeException("Workout not found"));
        CurrentWorkoutDTO currentWorkoutDTO=new CurrentWorkoutDTO();
        currentWorkoutDTO.setId(workout.getId());
        currentWorkoutDTO.setWorkoutExercises(new ArrayList<>());
        for(WorkoutExercise workoutExercise:workout.getWorkoutExercises()){
            CurrentWorkoutExerciseDTO currentWorkoutExerciseDTO=new CurrentWorkoutExerciseDTO();
            currentWorkoutExerciseDTO.setId(workoutExercise.getId());
            currentWorkoutExerciseDTO.setExercise(new ExerciseDetailsDTO(workoutExercise.getExercise().getId(),workoutExercise.getExercise().getTitle(),workoutExercise.getExercise().getDescription()));
            currentWorkoutExerciseDTO.setMinimumRestTime(workoutExercise.getMinimumRestTime());
            currentWorkoutExerciseDTO.setMaximumRestTime(workoutExercise.getMaximumRestTime());
            currentWorkoutExerciseDTO.setSets(new ArrayList<>());
            currentWorkoutDTO.getWorkoutExercises().add(currentWorkoutExerciseDTO);
            for(Set set:workoutExercise.getSets()){
                CurrentSetDTO setDetailsDTO=new CurrentSetDTO();
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
                VolumeMetricDetailsDTO volumeMetricDTO = new VolumeMetricDetailsDTO();
                volumeMetricDTO.setId(set.getVolumeMetric().getId());
                volumeMetricDTO.setRange(set.getVolumeMetric().isRange());
                volumeMetricDTO.setTitle(set.getVolumeMetric().getTitle());
                volumeMetricDTO.setMetricSymbol(set.getVolumeMetric().getMetricSymbol());
                setDetailsDTO.setVolumeMetric(volumeMetricDTO);

                // Map intensity metric
                IntensityMetricDetailsDTO intensityMetricDTO = new IntensityMetricDetailsDTO();
                intensityMetricDTO.setId(set.getIntensityMetric().getId());
                intensityMetricDTO.setMinimumIntensity(set.getIntensityMetric().getMinimumIntensity());
                intensityMetricDTO.setMaximumIntensity(set.getIntensityMetric().getMaximumIntensity());
                intensityMetricDTO.setRange(set.getIntensityMetric().isRange());
                intensityMetricDTO.setTitle(set.getIntensityMetric().getTitle());
                intensityMetricDTO.setMetricSymbol(set.getIntensityMetric().getMetricSymbol());
                setDetailsDTO.setIntensityMetric(intensityMetricDTO);
                currentWorkoutExerciseDTO.getSets().add(setDetailsDTO);

            }
        }
        return currentWorkoutDTO;
    }

    public StartProgramResponseDTO startProgram(Long programId,String username) {
        Program program=programRepository.findById(programId).orElseThrow(() -> new RuntimeException("Program not found!"));
        User user=userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found!"));
        StartedProgram startedProgram=new StartedProgram();
        startedProgram.setProgram(program);
        startedProgram.setStartDate(new Date());
        startedProgramRepository.save(startedProgram);
        user.getStartedPrograms().add(startedProgram);
        userRepository.save(user);
        return new StartProgramResponseDTO(startedProgram.getId(),program.getTitle());

    }
}