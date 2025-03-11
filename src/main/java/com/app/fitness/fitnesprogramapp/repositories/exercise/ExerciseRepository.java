package com.app.fitness.fitnesprogramapp.repositories.exercise;

import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository  extends JpaRepository<Exercise, Long> {
}
