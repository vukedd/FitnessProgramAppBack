package com.app.fitness.fitnesprogramapp.controllers.volume;

import com.app.fitness.fitnesprogramapp.dtos.volume.DailyVolumeReportDTO;
import com.app.fitness.fitnesprogramapp.dtos.volume.DailyWorkoutDurationReportDTO;
import com.app.fitness.fitnesprogramapp.repositories.workout.StartedWorkoutRepository;
import com.app.fitness.fitnesprogramapp.services.program.ProgramHistoryService;
import com.app.fitness.fitnesprogramapp.services.program.VolumeService;
import com.app.fitness.fitnesprogramapp.services.workout.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/volume")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200/")
public class VolumeController {
    private final VolumeService volumeService;
    private final ProgramHistoryService programHistoryService;

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

    @GetMapping("/weekly-volume")
    public ResponseEntity<List<DailyVolumeReportDTO>> getProgramWeeklyVolume(@RequestParam LocalDate startDate
            , @RequestParam LocalDate endDate, Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(programHistoryService.getWeeklyVolumeReport(username, startDate, endDate));
    }

    @GetMapping("/weekly-workout-duration-report")
    public ResponseEntity<List<DailyWorkoutDurationReportDTO>> getWeeklyWorkoutDurationReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(programHistoryService.getWeeklyWorkoutDurationReport(username, startDate, endDate));
    }
}
