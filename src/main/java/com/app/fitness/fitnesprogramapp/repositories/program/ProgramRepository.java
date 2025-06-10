package com.app.fitness.fitnesprogramapp.repositories.program;

import com.app.fitness.fitnesprogramapp.models.program.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {

    // This JPQL query is already cross-database compatible
    @Query("SELECT p FROM Program p WHERE p.title ILIKE :title AND p.isPublic = true")
    Page<Program> searchByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT p FROM Program p WHERE p.creator.username = :username")
    Page<Program> findByCreatorUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT p FROM Program p WHERE p.creator.username = :username AND LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Program> findByCreatorUsernameAndTitleContainingIgnoreCase(
            @Param("username") String username,
            @Param("title") String title,
            Pageable pageable);

    @EntityGraph(attributePaths = {
            "weeks.workouts.workoutExercises.sets.volumeMetric",
            "weeks.workouts.workoutExercises.sets.intensityMetric",
            "weeks.workouts.workoutExercises.exercise"
    })
    Optional<Program> findFullProgramById(Long id);

}
