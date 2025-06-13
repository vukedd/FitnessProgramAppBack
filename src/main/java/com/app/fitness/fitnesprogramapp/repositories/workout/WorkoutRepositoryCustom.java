package com.app.fitness.fitnesprogramapp.repositories.workout;

import com.app.fitness.fitnesprogramapp.models.workout.Workout;

import java.util.Optional;

public interface WorkoutRepositoryCustom {
    Optional<Workout> findAndInitializeById(Long id);
}
