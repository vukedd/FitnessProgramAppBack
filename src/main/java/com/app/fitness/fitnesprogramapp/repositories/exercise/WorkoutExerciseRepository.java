package com.app.fitness.fitnesprogramapp.repositories.exercise;

import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
    @Modifying
    @Query("DELETE FROM WorkoutExercise we WHERE we.id IN (SELECT w_e.id FROM Program p JOIN p.weeks w JOIN w.workouts wo JOIN wo.workoutExercises w_e WHERE p.id = :programId)")
    void bulkDeleteByProgramId(@Param("programId") Long programId);
}
