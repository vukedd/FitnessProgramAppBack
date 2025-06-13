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

        List<Week> weeksWithWorkouts = entityManager.createQuery(
                        "SELECT DISTINCT w FROM Week w LEFT JOIN FETCH w.workouts WHERE w IN :weeks", Week.class)
                .setParameter("weeks", weeks)
                .getResultList();

        Set<Workout> workouts = weeksWithWorkouts.stream()
                .flatMap(w -> w.getWorkouts().stream())
                .collect(Collectors.toSet());

        if (workouts.isEmpty()) {
            return Optional.of(program);
        }

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

        entityManager.createQuery(
                        "SELECT DISTINCT we FROM WorkoutExercise we LEFT JOIN FETCH we.sets WHERE we IN :workoutExercises", WorkoutExercise.class)
                .setParameter("workoutExercises", workoutExercises)
                .getResultList();

        return Optional.of(program);
    }
}
