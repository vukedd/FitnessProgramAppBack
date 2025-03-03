package com.app.fitness.fitnesprogramapp.repositories.program;

import com.app.fitness.fitnesprogramapp.models.program.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
}
