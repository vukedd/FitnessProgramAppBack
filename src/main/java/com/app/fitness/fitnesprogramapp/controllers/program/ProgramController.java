package com.app.fitness.fitnesprogramapp.controllers.program;

import com.app.fitness.fitnesprogramapp.dtos.program.ProgramCreateDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.ProgramOverviewDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.currentworkout.CurrentWorkoutDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.details.ProgramDetailsDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateExerciseHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateExerciseHistoryResponseDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.ProgramHistoryDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.startprogram.StartProgramResponseDTO;
import com.app.fitness.fitnesprogramapp.models.program.Program;
import com.app.fitness.fitnesprogramapp.services.program.ProgramHistoryService;
import com.app.fitness.fitnesprogramapp.services.program.ProgramService;
import com.app.fitness.fitnesprogramapp.services.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200/")
public class ProgramController {
    private final ProgramService programService;
    private final UserService userService;
    private final ProgramHistoryService programHistoryService;

    @GetMapping
    public ResponseEntity<Page<ProgramOverviewDTO>> getAllPrograms(
            @PageableDefault(size = 4, sort = "followersNumber", direction = Sort.Direction.DESC) Pageable programsPage
    ) {
        Page<ProgramOverviewDTO> programs = programService.getAllProgramsOverview(programsPage);
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProgramOverviewDTO>> searchPrograms(
            @PageableDefault(size = 4, sort = "followersNumber", direction = Sort.Direction.DESC) Pageable programsPage,
            @RequestParam("title") String title) {
        Page<ProgramOverviewDTO> programs = programService.searchProgramsByTitle(title, programsPage);
        return ResponseEntity.ok(programs);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProgram(
            @RequestPart("program") String programJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication) {

            // Get current authenticated user
            String username = authentication.getName();
            ProgramCreateDTO programDTO = programService.convertJsonToDTO(programJson);

            // Create program with the provided data
            Program createdProgram = programService.createProgram(programDTO, image, username);

            // Return the created program ID
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("id", createdProgram.getId(), "message", "Program created successfully"));

    }

    @PostMapping("/start-program/{programId}")
    public ResponseEntity<StartProgramResponseDTO> startProgram(
        @PathVariable Long programId,
        Authentication authentication
    )
    {
        String username = authentication.getName();
        return ResponseEntity.ok(programService.startProgram(programId,username));
    }

    @GetMapping("/started")
    public ResponseEntity<Page<ProgramOverviewDTO>> getStartedPrograms(Authentication authentication,
                                                                       @PageableDefault(size = 4, sort = "followersNumber", direction = Sort.Direction.DESC) Pageable programsPage) {
        String username = authentication.getName();
        return ResponseEntity.ok(programService.getStartedProgramsOverview(programsPage,username));

    }

    @GetMapping("/history/{startedProgramId}")
    public ResponseEntity<ProgramHistoryDTO> getProgramHistory(@PathVariable Long startedProgramId,Authentication authentication){
        String username = authentication.getName();
        return ResponseEntity.ok(programHistoryService.getProgramHistory(startedProgramId,username));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProgramDetailsDTO> getProgramById(@PathVariable Long id) {
        //add authentication here
        ProgramDetailsDTO program = programService.getProgramDetails(id);
        return ResponseEntity.ok(program);
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> getProgramImage(@PathVariable Long id) {
        return ResponseEntity.ok(programService.getProgramImage(id));
    }
}
