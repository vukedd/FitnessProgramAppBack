package com.app.fitness.fitnesprogramapp.repositories.workout;

import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @Query(value = """
    SELECT DATE(sw.done_date) as 'Date',
        ROUND(SUM(TIME_TO_SEC(TIMEDIFF(sw.done_date, sw.start_date)) / 3600), 2) as 'Hours'
    FROM started_workout sw
    JOIN started_week_started_workouts swsw ON swsw.started_workouts_id = sw.id
    JOIN started_week sw2 ON sw2.id = swsw.started_week_id
    JOIN started_program_started_weeks spsw ON spsw.started_weeks_id = sw2.id
    JOIN started_program sp ON sp.id = spsw.started_program_id
    JOIN users_started_programs usp ON usp.started_program_id = sp.id
    JOIN users u ON u.id = usp.user_id
    WHERE u.username = :username
    AND DATE(sw.done_date) BETWEEN :startDate AND :endDate
    GROUP BY DATE(sw.done_date), u.username
    ORDER BY DATE(sw.done_date) DESC;
    """, nativeQuery = true)
    List<Object[]> fetchWeeklyWorkoutDurationReport (
            @Param("username") String username,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
