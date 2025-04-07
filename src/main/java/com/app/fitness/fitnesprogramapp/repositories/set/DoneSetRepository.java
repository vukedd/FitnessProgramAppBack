package com.app.fitness.fitnesprogramapp.repositories.set;

import com.app.fitness.fitnesprogramapp.models.set.DoneSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DoneSetRepository extends JpaRepository<DoneSet, Long> {
    @Query(value = "SELECT " +
            "    DATE(ds.date) as day, " +
            "    COUNT(ds.id) as sets_count " +
            "FROM " +
            "    users u, users_started_programs usp, started_program sp, " +
            "    started_program_started_weeks spsw, started_week sw, " +
            "    started_week_started_workouts swsw, started_workout sw2, " +
            "    started_workout_done_sets swds, done_sets ds " +
            "WHERE " +
            "    u.id = usp.user_id " +
            "    AND usp.started_program_id = sp.id " +
            "    AND sp.id = spsw.started_program_id " +
            "    AND spsw.started_weeks_id = sw.id " +
            "    AND sw.id = swsw.started_week_id " +
            "    AND swsw.started_workouts_id = sw2.id " +
            "    AND sw2.id = swds.started_workout_id " +
            "    AND swds.done_sets_id = ds.id " +
            "    AND sw2.finished = 1 " +
            "    AND u.username = :username " +
            "    AND ds.date BETWEEN :startDate AND :endDate " +
            "GROUP BY " +
            "    DATE(ds.date) " +
            "ORDER BY " +
            "    day DESC",
            nativeQuery = true)
    List<Object[]> fetchDailyVolumeByUserForDateRange(@Param("username") String username,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);


}
