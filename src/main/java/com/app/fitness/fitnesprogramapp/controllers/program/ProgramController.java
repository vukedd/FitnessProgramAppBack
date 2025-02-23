package com.app.fitness.fitnesprogramapp.controllers.program;

import com.app.fitness.fitnesprogramapp.dtos.program.ProgramOverviewDTO;
import com.app.fitness.fitnesprogramapp.services.program.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
@CrossOrigin
public class ProgramController {
    private final ProgramService programService;
    @GetMapping
    public ResponseEntity<List<ProgramOverviewDTO>> getAllPrograms() {
        List<ProgramOverviewDTO> programs = programService.getAllPrograms();
        return ResponseEntity.ok(programs);
    }
}
