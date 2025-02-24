package com.app.fitness.fitnesprogramapp.controllers.program;

import com.app.fitness.fitnesprogramapp.dtos.program.ProgramOverviewDTO;
import com.app.fitness.fitnesprogramapp.services.program.ProgramService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<Page<ProgramOverviewDTO>> getAllPrograms(
            @PageableDefault(size = 4, sort = "followersNumber", direction = Sort.Direction.DESC) Pageable programsPage
    ) {
        Page<ProgramOverviewDTO> programs = programService.getAllPrograms(programsPage);
        return ResponseEntity.ok(programs);
    }
}
