package com.app.fitness.fitnesprogramapp.repositories.workout;

import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
}
