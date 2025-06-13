package com.app.fitness.fitnesprogramapp.repositories.set;

import com.app.fitness.fitnesprogramapp.models.set.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SetRepository extends JpaRepository<Set, Long> {
    @Modifying
    @Query("DELETE FROM Set s WHERE s.workoutExercise.workout.week.program.id = :programId")
    void bulkDeleteByProgramId(@Param("programId") Long programId);
}
