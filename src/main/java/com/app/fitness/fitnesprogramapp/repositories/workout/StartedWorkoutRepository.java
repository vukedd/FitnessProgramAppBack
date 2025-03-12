package com.app.fitness.fitnesprogramapp.repositories.workout;

import com.app.fitness.fitnesprogramapp.models.set.DoneSet;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StartedWorkoutRepository extends JpaRepository<StartedWorkout, Long> {
}
