package com.app.fitness.fitnesprogramapp.repositories.startedprogram;

import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.program.StartedProgram;
import com.app.fitness.fitnesprogramapp.models.week.StartedWeek;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class StartedProgramRepositoryCustomImpl implements StartedProgramRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<StartedProgram> findAndInitializeById(Long id) {
        List<StartedProgram> programs = entityManager.createQuery(
                        "SELECT DISTINCT p FROM StartedProgram p LEFT JOIN FETCH p.startedWeeks WHERE p.id = :id", StartedProgram.class)
                .setParameter("id", id)
                .getResultList();
        if (programs.isEmpty()) {
            return Optional.empty();
        }
        StartedProgram program = programs.get(0);
        java.util.Set<StartedWeek> weeks = new HashSet<>(program.getStartedWeeks());
        if (weeks.isEmpty()) {
            return Optional.of(program); // Nothing more to fetch
        }

        List<StartedWeek> weeksWithWorkouts = entityManager.createQuery(
                        "SELECT DISTINCT w FROM StartedWeek w LEFT JOIN FETCH w.startedWorkouts WHERE w IN :weeks", StartedWeek.class)
                .setParameter("weeks", weeks)
                .getResultList();

        Set<StartedWorkout> workouts = weeksWithWorkouts.stream()
                .flatMap(w -> w.getStartedWorkouts().stream())
                .collect(Collectors.toSet());

        if (workouts.isEmpty()) {
            return Optional.of(program);
        }

        List<StartedWorkout> workoutsWithExercises = entityManager.createQuery(
                        "SELECT DISTINCT w FROM StartedWorkout w LEFT JOIN FETCH w.workoutExercises we LEFT JOIN FETCH we.exercise WHERE w IN :workouts", StartedWorkout.class)
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

        entityManager.createQuery(
                        "SELECT DISTINCT sw FROM StartedWorkout sw " +
                                "LEFT JOIN FETCH sw.doneSets " +
                                "WHERE sw IN :workouts", StartedWorkout.class)
                .setParameter("workouts", workouts)
                .getResultList();

        return Optional.of(program);
    }
}
