package com.app.fitness.fitnesprogramapp.repositories.week;

import com.app.fitness.fitnesprogramapp.models.week.Week;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeekRepository extends JpaRepository<Week, Long> {
    @Modifying
    @Query("DELETE FROM Week w WHERE w.id IN (SELECT wk.id FROM Program p JOIN p.weeks wk WHERE p.id = :programId)")
    void bulkDeleteByProgramId(@Param("programId") Long programId);
}
