package com.app.fitness.fitnesprogramapp.repositories.workout;

import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @Query(value = """
    SELECT
        sw.done_date::date AS "Date",  -- PostgreSQL date extraction and standard alias
        ROUND(
            SUM(
                EXTRACT(EPOCH FROM (sw.done_date - sw.start_date)) / 3600.0 -- Calculate difference, get seconds, convert to hours
            ), 2
        ) AS "Hours" -- Standard alias
    FROM
        started_workout sw
    JOIN started_week_started_workouts swsw ON swsw.started_workouts_id = sw.id
    JOIN started_week sw2 ON sw2.id = swsw.started_week_id -- sw2 is an alias for started_week
    JOIN started_program_started_weeks spsw ON spsw.started_weeks_id = sw2.id
    JOIN started_program sp ON sp.id = spsw.started_program_id
    JOIN users_started_programs usp ON usp.started_program_id = sp.id
    JOIN users u ON u.id = usp.user_id
    WHERE
        u.username = :username
        AND sw.done_date::date BETWEEN :startDate ::date AND :endDate ::date -- PostgreSQL date extraction and parameter casting
    GROUP BY
        sw.done_date::date, u.username -- u.username in GROUP BY is redundant if already filtered by WHERE, but harmless
    ORDER BY
        sw.done_date::date DESC
    """, nativeQuery = true)
    List<Object[]> fetchWeeklyWorkoutDurationReport (
            @Param("username") String username,
            @Param("startDate") String startDate, // Ensure this string is in 'YYYY-MM-DD' format
            @Param("endDate") String endDate     // Ensure this string is in 'YYYY-MM-DD' format
    );

    @Modifying
    @Query("DELETE FROM Workout w WHERE w.id IN (SELECT wo.id FROM Program p JOIN p.weeks wk JOIN wk.workouts wo WHERE p.id = :programId)")
    void bulkDeleteByProgramId(@Param("programId") Long programId);
}
