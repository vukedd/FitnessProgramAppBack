package com.app.fitness.fitnesprogramapp.repositories.program;

import com.app.fitness.fitnesprogramapp.models.program.StartedProgram;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StartedProgramRepository extends JpaRepository<StartedProgram, Long> {
}
