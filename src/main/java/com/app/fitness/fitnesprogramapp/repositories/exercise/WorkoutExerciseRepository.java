package com.app.fitness.fitnesprogramapp.repositories.exercise;

import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
}
