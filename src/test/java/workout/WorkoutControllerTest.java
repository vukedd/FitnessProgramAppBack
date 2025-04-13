package workout;

import com.app.fitness.fitnesprogramapp.FitnesProgramAppApplication;
import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateDoneSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.update.*;
import com.app.fitness.fitnesprogramapp.dtos.workout.CompleteWorkoutResponseDTO;
import com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutDTO;
import com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutDetailsDTO;
import com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutDoneSetDTO;

import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.program.Program;
import com.app.fitness.fitnesprogramapp.models.program.StartedProgram;
import com.app.fitness.fitnesprogramapp.models.set.*;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.models.week.Week;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import com.app.fitness.fitnesprogramapp.repositories.exercise.ExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.exercise.WorkoutExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.metrics.IntensityMetricRepository;
import com.app.fitness.fitnesprogramapp.repositories.metrics.VolumeMetricRepository;
import com.app.fitness.fitnesprogramapp.repositories.program.ProgramRepository;
import com.app.fitness.fitnesprogramapp.repositories.program.StartedProgramRepository;
import com.app.fitness.fitnesprogramapp.repositories.set.DoneSetRepository;
import com.app.fitness.fitnesprogramapp.repositories.set.SetRepository;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import com.app.fitness.fitnesprogramapp.repositories.week.StartedWeekRepository;
import com.app.fitness.fitnesprogramapp.repositories.week.WeekRepository;
import com.app.fitness.fitnesprogramapp.repositories.workout.StartedWorkoutRepository;
import com.app.fitness.fitnesprogramapp.repositories.workout.WorkoutRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = FitnesProgramAppApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("jpatest")
@Import(TestSecurityConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class WorkoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private WeekRepository weekRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private WorkoutExerciseRepository workoutExerciseRepository;

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private StartedProgramRepository startedProgramRepository;

    @Autowired
    private StartedWeekRepository startedWeekRepository;

    @Autowired
    private StartedWorkoutRepository startedWorkoutRepository;

    @Autowired
    private DoneSetRepository doneSetRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private VolumeMetricRepository volumeMetricRepository;

    @Autowired
    private IntensityMetricRepository intensityMetricRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Program testProgram;
    private Week testWeek;
    private Workout testWorkout;
    private StartedProgram startedProgram;
    private Long workoutExerciseId;
    private Long setId;
    private Exercise exercise;
    private IntensityMetric intensityMetric;
    private VolumeMetric volumeMetric;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);

        // Create test program
        testProgram = new Program();
        testProgram.setTitle("Test Program");
        testProgram.setWeeks(new ArrayList<>());
        testProgram.setCreator(testUser);
        testProgram = programRepository.save(testProgram);

        // Create test week
        testWeek = new Week();
        testWeek.setWorkouts(new ArrayList<>());
        testWeek = weekRepository.save(testWeek);

        // Add week to program
        testProgram.getWeeks().add(testWeek);
        testProgram = programRepository.save(testProgram);

        // Create test workout
        testWorkout = new Workout();
        testWorkout.setTitle("Test Workout");
        testWorkout.setDescription("Test Workout Description");
        testWorkout.setNumber("1");
        testWorkout.setWorkoutExercises(new ArrayList<>());
        testWorkout = workoutRepository.save(testWorkout);

        // Add workout to week
        testWeek.getWorkouts().add(testWorkout);
        testWeek = weekRepository.save(testWeek);

        // Create and save the exercise
        exercise = new Exercise();
        exercise.setTitle("Test Exercise");
        exercise.setDescription("Test Exercise Description");
        exercise.setExerciseMuscles(new ArrayList<>());
        exercise = exerciseRepository.save(exercise);

        // Create metrics
        intensityMetric = new IntensityMetric();
        intensityMetric.setMaximumIntensity(10);
        intensityMetric.setMinimumIntensity(10);
        intensityMetric.setMetricSymbol("rpe");
        intensityMetric.setRange(false);
        intensityMetric = intensityMetricRepository.save(intensityMetric);

        volumeMetric = new VolumeMetric();
        volumeMetric.setMetricSymbol("reps");
        volumeMetric.setRange(false);
        volumeMetric.setTitle("Repetitions");
        volumeMetric = volumeMetricRepository.save(volumeMetric);

        // Create workout exercise with exercise
        WorkoutExercise workoutExercise = new WorkoutExercise();
        workoutExercise.setExercise(exercise);
        workoutExercise.setMinimumRestTime(60);
        workoutExercise.setMaximumRestTime(120);
        workoutExercise.setSets(new ArrayList<>());

        // Create test set
        Set set = new Set();
        SetIntensity intensity = new SetIntensity();
        intensity.setMinimumIntensity(70);
        intensity.setMaximumIntensity(80);
        set.setIntensity(intensity);
        set.setIntensityMetric(intensityMetric);
        set.setVolumeMetric(volumeMetric);

        SetVolume volume = new SetVolume();
        volume.setMinimumVolume(8);
        volume.setMaximumVolume(12);
        set.setVolume(volume);

        set = setRepository.save(set);
        setId = set.getId();

        // Add the set to workout exercise and save
        workoutExercise.getSets().add(set);
        workoutExercise = workoutExerciseRepository.save(workoutExercise);
        workoutExerciseId = workoutExercise.getId();

        // Add workout exercise to workout
        testWorkout.getWorkoutExercises().add(workoutExercise);
        testWorkout = workoutRepository.save(testWorkout);

        // Create started program
        startedProgram = new StartedProgram();
        startedProgram.setProgram(testProgram);
        startedProgram.setStartDate(new Date());
        startedProgram.setFinished(false);
        startedProgram.setStartedWeeks(new ArrayList<>());
        startedProgram = startedProgramRepository.save(startedProgram);

        testUser.getStartedPrograms().add(startedProgram);
        testUser = userRepository.save(testUser);
    }

    // Helper method to create a program update DTO
    private ProgramUpdateDTO createProgramUpdateDTO() {
        ProgramUpdateDTO updateDTO = new ProgramUpdateDTO();
        updateDTO.setId((int) testProgram.getId().longValue());
        updateDTO.setName(testProgram.getTitle());

        List<UpdateWeekDTO> weekDTOs = new ArrayList<>();
        for (Week week : testProgram.getWeeks()) {
            UpdateWeekDTO weekDTO = new UpdateWeekDTO();
            weekDTO.setId((int) week.getId().longValue());

            List<UpdateWorkoutDTO> workoutDTOs = new ArrayList<>();
            for (Workout workout : week.getWorkouts()) {
                UpdateWorkoutDTO workoutDTO = new UpdateWorkoutDTO();
                workoutDTO.setId((int) workout.getId().longValue());
                workoutDTO.setTitle(workout.getTitle());
                workoutDTO.setDescription(workout.getDescription());
                workoutDTO.setNumber(workout.getNumber());

                List<UpdateWorkoutExerciseDTO> exerciseDTOs = new ArrayList<>();
                for (WorkoutExercise we : workout.getWorkoutExercises()) {
                    List<UpdateWorkoutExerciseSetDTO> setDTOs = new ArrayList<>();
                    for (Set set : we.getSets()) {
                        UpdateWorkoutExerciseSetDTO setDTO = new UpdateWorkoutExerciseSetDTO();
                        setDTO.setId((int) set.getId().longValue());
                        setDTO.setVolumeMin(set.getVolume().getMinimumVolume());
                        setDTO.setVolumeMax(set.getVolume().getMaximumVolume());
                        setDTO.setVolumeMetric(set.getVolumeMetric().getId());
                        setDTO.setIntensityMetric(set.getIntensityMetric().getId());
                        setDTO.setIntensityMin(set.getIntensity().getMinimumIntensity());
                        setDTO.setIntensityMax(set.getIntensity().getMaximumIntensity());
                        setDTOs.add(setDTO);
                    }

                    UpdateWorkoutExerciseDTO exerciseDTO = new UpdateWorkoutExerciseDTO(
                            (int) we.getId().longValue(),
                            we.getExercise().getId(),
                            we.getMinimumRestTime(),
                            we.getMaximumRestTime(),
                            setDTOs
                    );
                    exerciseDTOs.add(exerciseDTO);
                }

                workoutDTO.setWorkoutExercises(exerciseDTOs);
                workoutDTOs.add(workoutDTO);
            }

            weekDTO.setWorkouts(workoutDTOs);
            weekDTOs.add(weekDTO);
        }

        updateDTO.setWeeks(weekDTOs);
        return updateDTO;
    }

    @Test
    void getNextWorkout_ShouldReturnFirstWorkout_WhenProgramStarted() throws Exception {
        // Send request to get next workout
        MvcResult result = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response
        NextWorkoutDTO nextWorkoutDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(nextWorkoutDTO);
        assertEquals("start", nextWorkoutDTO.getAction());

        NextWorkoutDetailsDTO details = nextWorkoutDTO.getNextWorkoutDetails();
        assertNotNull(details);
        assertEquals(testWorkout.getId(), details.getWorkoutId());
        assertEquals(testWorkout.getTitle(), details.getTitle());
        assertEquals(testWorkout.getDescription(), details.getDescription());
        assertEquals(testWorkout.getNumber(), details.getNumber());
    }

    @Test
    @Transactional
    void completeSet_ShouldReturnSuccess_WhenSetCompleted() throws Exception {
        // First get next workout to start it
        MvcResult nextWorkoutResult = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        NextWorkoutDTO nextWorkoutDTO = objectMapper.readValue(
                nextWorkoutResult.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(nextWorkoutDTO);

        Long startedWorkoutId = nextWorkoutDTO.getNextWorkoutDetails().getId();
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElse(null);
        assertNotNull(startedWorkout);

        // Create done set DTO
        CreateDoneSetDTO doneSetDTO = new CreateDoneSetDTO();
        doneSetDTO.setStartedWorkoutId(startedWorkoutId);
        doneSetDTO.setWorkoutExerciseId(workoutExerciseId);
        doneSetDTO.setSetId(setId);
        doneSetDTO.setVolume(10);
        doneSetDTO.setIntensity(75);
        doneSetDTO.setWeightLifted(100.0);

        // Send request to complete set
        MvcResult result = mockMvc.perform(post("/api/workouts/completeSet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doneSetDTO)))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response
        NextWorkoutDoneSetDTO doneSetResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                NextWorkoutDoneSetDTO.class);

        assertNotNull(doneSetResponse);
        assertEquals(10, doneSetResponse.getVolume());
        assertEquals(75, doneSetResponse.getIntensity());
        assertEquals(100.0, doneSetResponse.getWeightLifted());

        // Verify done set was saved
        startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElse(null);
        assertNotNull(startedWorkout);
        assertEquals(1, startedWorkout.getDoneSets().size());
    }

    @Test
    void completeWorkout_ShouldReturnSuccess_WhenWorkoutCompleted() throws Exception {
        // First get next workout to start it
        MvcResult nextWorkoutResult = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        NextWorkoutDTO nextWorkoutDTO = objectMapper.readValue(
                nextWorkoutResult.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(nextWorkoutDTO);

        Long startedWorkoutId = nextWorkoutDTO.getNextWorkoutDetails().getId();

        // Send request to complete workout
        MvcResult result = mockMvc.perform(post("/api/workouts/completeWorkout/{workoutId}/{startedProgramId}",
                        startedWorkoutId, startedProgram.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response
        CompleteWorkoutResponseDTO completeWorkoutResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CompleteWorkoutResponseDTO.class);

        assertNotNull(completeWorkoutResponse);
        assertEquals(startedWorkoutId, completeWorkoutResponse.getStartedWorkoutId());
        assertEquals("Successfully completed workout!", completeWorkoutResponse.getMessage());

        // Verify workout was marked as completed
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElse(null);
        assertNotNull(startedWorkout);
        assertTrue(startedWorkout.isFinished());
        assertNotNull(startedWorkout.getDoneDate());
    }

    @Test
    @Transactional
    void uncompleteSet_ShouldReturnSuccess_WhenSetRemoved() throws Exception {
        // First get next workout to start it
        MvcResult nextWorkoutResult = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        NextWorkoutDTO nextWorkoutDTO = objectMapper.readValue(
                nextWorkoutResult.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(nextWorkoutDTO);

        Long startedWorkoutId = nextWorkoutDTO.getNextWorkoutDetails().getId();

        // Complete a set first
        CreateDoneSetDTO doneSetDTO = new CreateDoneSetDTO();
        doneSetDTO.setStartedWorkoutId(startedWorkoutId);
        doneSetDTO.setWorkoutExerciseId(workoutExerciseId);
        doneSetDTO.setSetId(setId);
        doneSetDTO.setVolume(10);
        doneSetDTO.setIntensity(75);
        doneSetDTO.setWeightLifted(100.0);

        MvcResult completeSetResult = mockMvc.perform(post("/api/workouts/completeSet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doneSetDTO)))
                .andExpect(status().isOk())
                .andReturn();

        NextWorkoutDoneSetDTO doneSetResponse = objectMapper.readValue(
                completeSetResult.getResponse().getContentAsString(),
                NextWorkoutDoneSetDTO.class);

        assertNotNull(doneSetResponse);
        Long doneSetId = doneSetResponse.getId();

        // Send request to uncomplete the set
        MvcResult result = mockMvc.perform(delete("/api/workouts/uncompleteSet/{startedWorkoutId}/{doneSetId}",
                        startedWorkoutId, doneSetId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response includes expected message
        String responseString = result.getResponse().getContentAsString();
        assertTrue(responseString.contains("Successfully removed completed set from workout!"));

        // Verify done set was removed
        StartedWorkout startedWorkout = startedWorkoutRepository.findById(startedWorkoutId).orElse(null);
        assertNotNull(startedWorkout);
        assertEquals(0, startedWorkout.getDoneSets().size());
    }

    @Test
    @Transactional
    void getNextWorkout_ShouldReturnWorkout_WhenWorkoutWasDeleted() throws Exception {
        // First get next workout to start it
        MvcResult nextWorkoutResult = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        NextWorkoutDTO nextWorkoutDTO = objectMapper.readValue(
                nextWorkoutResult.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(nextWorkoutDTO);

        // Instead of directly deleting, update the program to remove the workout
        testProgram.getWeeks().get(0).getWorkouts().removeFirst();
        ProgramUpdateDTO updateDTO = createProgramUpdateDTO();

        // Update the program through the API
        mockMvc.perform(multipart("/api/programs/{id}", testProgram.getId())
                        .file(new MockMultipartFile("program", "", "application/json",
                                objectMapper.writeValueAsString(updateDTO).getBytes()))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser")))  // Add authentication
                .andExpect(status().isOk());

        // Get the next workout again - should still be the started workout
        MvcResult secondResult = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response
        NextWorkoutDTO secondNextWorkoutDTO = objectMapper.readValue(
                secondResult.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(secondNextWorkoutDTO);
        assertEquals("continue", secondNextWorkoutDTO.getAction());

        NextWorkoutDetailsDTO details = secondNextWorkoutDTO.getNextWorkoutDetails();
        assertNotNull(details);
        assertEquals(testWorkout.getId(), details.getWorkoutId());
        assertEquals(testWorkout.getTitle(), details.getTitle());
    }

    @Test
    @Transactional
    void getNextWorkout_ShouldContinueToNextWeek_WhenWeekWasDeleted() throws Exception {
        // Create a second week with workout
        Week secondWeek = new Week();
        secondWeek.setWorkouts(new ArrayList<>());
        secondWeek = weekRepository.save(secondWeek);

        // Add second week to program
        testProgram.getWeeks().add(secondWeek);
        testProgram = programRepository.save(testProgram);

        // Create workout for second week
        Workout secondWorkout = new Workout();
        secondWorkout.setTitle("Second Workout");
        secondWorkout.setDescription("Second Workout Description");
        secondWorkout.setNumber("1");
        secondWorkout.setWorkoutExercises(new ArrayList<>());
        secondWorkout = workoutRepository.save(secondWorkout);

        // Add workout to second week
        secondWeek.getWorkouts().add(secondWorkout);
        secondWeek = weekRepository.save(secondWeek);

        // First get next workout to start it
        MvcResult nextWorkoutResult = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        NextWorkoutDTO nextWorkoutDTO = objectMapper.readValue(
                nextWorkoutResult.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(nextWorkoutDTO);

        Long startedWorkoutId = nextWorkoutDTO.getNextWorkoutDetails().getId();

        // Complete the workout
        mockMvc.perform(post("/api/workouts/completeWorkout/{workoutId}/{startedProgramId}",
                        startedWorkoutId, startedProgram.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Update program to remove the first week
        testProgram.getWeeks().removeFirst();
        ProgramUpdateDTO updateDTO = createProgramUpdateDTO();


        // Update the program through the API
        mockMvc.perform(multipart("/api/programs/{id}", testProgram.getId())
                .file(new MockMultipartFile("program", "", "application/json",
                     objectMapper.writeValueAsString(updateDTO).getBytes()))
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .with(SecurityMockMvcRequestPostProcessors.user("testuser")))  // Add authentication
        .andExpect(status().isOk());

        // Get the next workout - should be from second week
        MvcResult secondResult = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response
        NextWorkoutDTO secondNextWorkoutDTO = objectMapper.readValue(
                secondResult.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(secondNextWorkoutDTO);
        assertEquals("start", secondNextWorkoutDTO.getAction());

        NextWorkoutDetailsDTO details = secondNextWorkoutDTO.getNextWorkoutDetails();
        assertNotNull(details);
        assertEquals(secondWorkout.getId(), details.getWorkoutId());
        assertEquals(secondWorkout.getTitle(), details.getTitle());
    }

    @Test
    @Transactional
    void getNextWorkout_ShouldReturnSameWorkout_WhenExercisesAndSetsWereDeleted() throws Exception {
        // First get next workout to start it
        MvcResult nextWorkoutResult = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        NextWorkoutDTO nextWorkoutDTO = objectMapper.readValue(
                nextWorkoutResult.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(nextWorkoutDTO);

        // Update program to remove exercises and sets
        testProgram.getWeeks().getFirst().getWorkouts().getFirst().getWorkoutExercises().removeFirst();
        ProgramUpdateDTO updateDTO = createProgramUpdateDTO();


        // Update the program through the API
        mockMvc.perform(multipart("/api/programs/{id}", testProgram.getId())
                .file(new MockMultipartFile("program", "", "application/json",
                     objectMapper.writeValueAsString(updateDTO).getBytes()))
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .with(SecurityMockMvcRequestPostProcessors.user("testuser")))  // Add authentication
        .andExpect(status().isOk());

        // Get the next workout again - should still have the exercises and sets in started workout
        MvcResult secondResult = mockMvc.perform(get("/api/workouts/next/{programId}", startedProgram.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Verify response
        NextWorkoutDTO secondNextWorkoutDTO = objectMapper.readValue(
                secondResult.getResponse().getContentAsString(),
                NextWorkoutDTO.class);

        assertNotNull(secondNextWorkoutDTO);
        assertEquals("continue", secondNextWorkoutDTO.getAction());

        NextWorkoutDetailsDTO details = secondNextWorkoutDTO.getNextWorkoutDetails();
        assertNotNull(details);
        assertEquals(1, details.getWorkoutExercises().size());
        assertEquals(1, details.getWorkoutExercises().get(0).getSets().size());
    }
}