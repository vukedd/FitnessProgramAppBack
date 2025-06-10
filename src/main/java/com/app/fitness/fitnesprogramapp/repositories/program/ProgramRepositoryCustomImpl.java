package com.app.fitness.fitnesprogramapp.repositories.program;

import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.program.Program;
import com.app.fitness.fitnesprogramapp.models.week.Week;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class ProgramRepositoryCustomImpl implements ProgramRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Program> findAndInitializeById(Long id) {
        // Step 1: Fetch the root entity (Program). We will fetch its 'weeks' in this first pass.
        // We use LEFT JOIN FETCH to ensure we get the program even if it has no weeks.
        List<Program> programs = entityManager.createQuery(
                        "SELECT DISTINCT p FROM Program p LEFT JOIN FETCH p.weeks WHERE p.id = :id", Program.class)
                .setParameter("id", id)
                .getResultList();

        if (programs.isEmpty()) {
            return Optional.empty();
        }

        Program program = programs.get(0);
        java.util.Set<Week> weeks = new HashSet<>(program.getWeeks());
        if (weeks.isEmpty()) {
            return Optional.of(program); // Nothing more to fetch
        }

        // Step 2: Second pass. Fetch the 'workouts' for all the weeks we just loaded.
        List<Week> weeksWithWorkouts = entityManager.createQuery(
                        "SELECT DISTINCT w FROM Week w LEFT JOIN FETCH w.workouts WHERE w IN :weeks", Week.class)
                .setParameter("weeks", weeks)
                .getResultList();

        // Collect all workouts from all weeks into a flat list for the next pass
        Set<Workout> workouts = weeksWithWorkouts.stream()
                .flatMap(w -> w.getWorkouts().stream())
                .collect(Collectors.toSet());

        if (workouts.isEmpty()) {
            return Optional.of(program);
        }

        // Step 3: Third pass. Fetch the 'workoutExercises' for all the workouts we just loaded.
        List<Workout> workoutsWithExercises = entityManager.createQuery(
                        "SELECT DISTINCT w FROM Workout w LEFT JOIN FETCH w.workoutExercises we LEFT JOIN FETCH we.exercise WHERE w IN :workouts", Workout.class)
                .setParameter("workouts", workouts)
                .getResultList();

        Set<WorkoutExercise> workoutExercises = workoutsWithExercises.stream()
                .flatMap(w -> w.getWorkoutExercises().stream())
                .collect(Collectors.toSet());

        if (workoutExercises.isEmpty()) {
            return Optional.of(program);
        }

        // Step 4: Fourth pass. Fetch the 'sets' for all workoutExercises.
        // This is the final level of the hierarchy.
        entityManager.createQuery(
                        "SELECT DISTINCT we FROM WorkoutExercise we LEFT JOIN FETCH we.sets WHERE we IN :workoutExercises", WorkoutExercise.class)
                .setParameter("workoutExercises", workoutExercises)
                .getResultList();

        // At this point, the 'program' object is fully initialized with all collections
        // loaded into the persistence context. No more lazy loading will occur.
        return Optional.of(program);
    }
}
