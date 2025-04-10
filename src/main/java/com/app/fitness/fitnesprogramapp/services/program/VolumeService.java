package com.app.fitness.fitnesprogramapp.services.program;

import com.app.fitness.fitnesprogramapp.dtos.exercise.history.ExerciseHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.exercise.history.WorkoutExerciseExerciseHistoryDTO;
import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import com.app.fitness.fitnesprogramapp.models.set.DoneSet;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.repositories.exercise.ExerciseRepository;
import com.app.fitness.fitnesprogramapp.repositories.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VolumeService {
    private final UserRepository userRepository;

    public Map<String, Double> getMuscleUsage(String username, Date startDate, Date endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Map<String, Double> muscleUsageMap = new HashMap<>();

        Stream<DoneSet> doneSetsStream = user.getStartedPrograms().stream()
                .flatMap(program -> program.getStartedWeeks().stream())
                .flatMap(week -> week.getStartedWorkouts().stream())
                .flatMap(workout -> workout.getDoneSets().stream());

        // Apply date filtering if provided
        if (startDate != null) {
            doneSetsStream = doneSetsStream.filter(doneSet ->
                    doneSet.getDate() != null && !doneSet.getDate().before(startDate));
        }

        if (endDate != null) {
            doneSetsStream = doneSetsStream.filter(doneSet ->
                    doneSet.getDate() != null && !doneSet.getDate().after(endDate));
        }

        doneSetsStream.forEach(doneSet -> {
            Exercise exercise = doneSet.getWorkoutExercise().getExercise();

            exercise.getExerciseMuscles().forEach(exerciseMuscle -> {
                String muscleName = exerciseMuscle.getMuscle().getName();
                int muscleIntensity = exerciseMuscle.getIntensity();

                // Calculate with double precision
                double actualIntensity =((double) muscleIntensity) /10;

                // Update the muscle usage map
                muscleUsageMap.compute(muscleName, (key, currentValue) ->
                        (currentValue == null) ? actualIntensity : currentValue + actualIntensity);
            });
        });

        return muscleUsageMap;
    }
}
