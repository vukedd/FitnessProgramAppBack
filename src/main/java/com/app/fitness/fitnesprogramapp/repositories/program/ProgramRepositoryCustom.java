package com.app.fitness.fitnesprogramapp.repositories.program;

import com.app.fitness.fitnesprogramapp.models.program.Program;

import java.util.Optional;

public interface ProgramRepositoryCustom {
    Optional<Program> findAndInitializeById(Long id);
}