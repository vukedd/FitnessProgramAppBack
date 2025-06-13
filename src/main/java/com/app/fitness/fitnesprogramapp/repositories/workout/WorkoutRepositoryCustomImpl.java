package com.app.fitness.fitnesprogramapp.repositories.workout;

import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WorkoutRepositoryCustomImpl implements WorkoutRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Workout> findAndInitializeById(Long id) {
        List<Workout> workouts = entityManager.createQuery(
                        "SELECT DISTINCT w FROM Workout w LEFT JOIN FETCH w.workoutExercises we LEFT JOIN FETCH we.exercise WHERE w.id = :id", Workout.class)
                .setParameter("id", id)
                .getResultList();
        if (workouts.isEmpty()) {
            return Optional.empty();
        }
        Workout workout = workouts.getFirst();

        Set<WorkoutExercise> workoutExercises = workouts.stream()
                .flatMap(w -> w.getWorkoutExercises().stream())
                .collect(Collectors.toSet());

        if (workoutExercises.isEmpty()) {
            return Optional.of(workout);
        }

        entityManager.createQuery(
                        "SELECT DISTINCT we FROM WorkoutExercise we LEFT JOIN FETCH we.sets WHERE we IN :workoutExercises", WorkoutExercise.class)
                .setParameter("workoutExercises", workoutExercises)
                .getResultList();

        return Optional.of(workout);
    }
}
