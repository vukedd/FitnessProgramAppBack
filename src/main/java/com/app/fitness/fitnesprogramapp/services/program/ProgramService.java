package com.app.fitness.fitnesprogramapp.services.program;

import com.app.fitness.fitnesprogramapp.dtos.program.ProgramOverviewDTO;
import com.app.fitness.fitnesprogramapp.models.program.Program;
import com.app.fitness.fitnesprogramapp.repositories.program.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramService {
    private final ProgramRepository programRepository;

//    public Program saveImage(MultipartFile file) throws IOException {
//        Program program = new Program();
//        program.setName(file.getOriginalFilename());
//        program.setImageData(file.getBytes());
//        return programRepository.save(program);
//    }

//    public Program getImage(Long id) {
//        return programRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Image not found"));
//    }

    public Page<ProgramOverviewDTO> getAllPrograms(Pageable pageable) {
        Page<Program> programPage = programRepository.findAll(pageable);
        return programPage.map(ProgramOverviewDTO::fromEntity);
    }
}