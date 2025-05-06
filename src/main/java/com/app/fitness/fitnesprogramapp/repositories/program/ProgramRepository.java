package com.app.fitness.fitnesprogramapp.repositories.program;

import com.app.fitness.fitnesprogramapp.models.program.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    @Query("SELECT p FROM Program p WHERE p.title LIKE :title")
    Page<Program> searchByTitle(@Param("title") String title, Pageable pageable);

    @Query(value = "SELECT followers_number,description, creator_id, p.id, title, image_data, is_public " +
            "FROM program p " +
            "JOIN users u ON u.id = p.creator_id " +
            "JOIN refresh_tokens rt ON rt.user_id = u.id " +
            "WHERE rt.id = :id AND rt.expires_at > CURRENT_DATE() AND p.title LIKE CONCAT('%', :title ,'%')",
            nativeQuery = true)
    Page<Program> findProgramsCreatedByMe(@Param("id") String refreshTokenId, @Param("title") String title , Pageable pageable);
}
