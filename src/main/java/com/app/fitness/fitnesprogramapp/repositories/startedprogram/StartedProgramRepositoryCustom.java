package com.app.fitness.fitnesprogramapp.repositories.startedprogram;

import com.app.fitness.fitnesprogramapp.models.program.StartedProgram;

import java.util.Optional;

public interface StartedProgramRepositoryCustom {
    Optional<StartedProgram> findAndInitializeById(Long id);
}
