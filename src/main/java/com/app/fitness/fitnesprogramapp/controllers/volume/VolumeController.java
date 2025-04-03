package com.app.fitness.fitnesprogramapp.controllers.volume;

import com.app.fitness.fitnesprogramapp.services.program.VolumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/volume")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200/")
public class VolumeController {
    private final VolumeService volumeService;
    @GetMapping("/per-muscle")
    public ResponseEntity<Map<String, Double>> getMuscleUsage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            Authentication authentication) {

        // Get username from Spring Security context
        String username = authentication.getName();

        Map<String, Double> muscleUsageMap = volumeService.getMuscleUsage(username, startDate, endDate);

        return ResponseEntity.ok(muscleUsageMap);
    }
}
