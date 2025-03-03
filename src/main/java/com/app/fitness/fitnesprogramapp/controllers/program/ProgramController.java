package com.app.fitness.fitnesprogramapp.controllers.program;

import com.app.fitness.fitnesprogramapp.dtos.program.ProgramCreateDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.ProgramOverviewDTO;
import com.app.fitness.fitnesprogramapp.models.program.Program;
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

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
@CrossOrigin
public class ProgramController {
    private final ProgramService programService;
    private final UserService userService;
    @GetMapping
    public ResponseEntity<Page<ProgramOverviewDTO>> getAllPrograms(
            @PageableDefault(size = 4, sort = "followersNumber", direction = Sort.Direction.DESC) Pageable programsPage
    ) {
        Page<ProgramOverviewDTO> programs = programService.getAllProgramsOverview(programsPage);
        return ResponseEntity.ok(programs);
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProgram(
            @RequestPart("program") String programJson,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication authentication) {
        try {
            // Get current authenticated user
            //String username = authentication.getName();
            ProgramCreateDTO programDTO = programService.convertJsonToDTO(programJson);

            // Create program with the provided data
            Program createdProgram = programService.createProgram(programDTO, image, "john_doe");

            // Return the created program ID
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("id", createdProgram.getId(), "message", "Program created successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error processing image file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error creating program: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Program> getProgramById(@PathVariable Long id) {
        Optional<Program> program = programService.getProgramById(id);
        return program.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProgramImage(@PathVariable Long id) {
        Optional<Program> programOpt = programService.getProgramById(id);
        if (programOpt.isPresent() && programOpt.get().getImageData() != null) {
            byte[] imageData = programOpt.get().getImageData();
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // or determine dynamically
                    .body(imageData);
        }
        return ResponseEntity.notFound().build();

    }
}
