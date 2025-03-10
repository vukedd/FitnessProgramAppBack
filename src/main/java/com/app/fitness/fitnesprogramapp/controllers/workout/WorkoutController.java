package com.app.fitness.fitnesprogramapp.controllers.workout;

import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateDoneSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutDTO;
import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import com.app.fitness.fitnesprogramapp.services.workout.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
@CrossOrigin
public class WorkoutController {
    private final WorkoutService workoutService;

    /**
     * Gets the next workout for a given program
     * @param programId The started program ID
     * @return The next workout information as a DTO
     */
    @GetMapping("/next/{programId}")
    public ResponseEntity<?> getNextWorkout(@PathVariable Long programId) {
        NextWorkoutDTO nextWorkoutDTO = workoutService.processNextWorkout(programId);

        if (nextWorkoutDTO == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(nextWorkoutDTO);
    }

    /**
     * Completes a set in a workout
     * @param workoutId The started workout ID
     * @param doneSetDTO The completed set data
     * @return The updated started workout
     */
    @PostMapping("/completeSet/{workoutId}")
    public ResponseEntity<?> completeSet(@PathVariable Long workoutId, @RequestBody CreateDoneSetDTO doneSetDTO) {
        try {
            StartedWorkout updatedWorkout = workoutService.processCompleteSet(workoutId, doneSetDTO);

            if (updatedWorkout == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(updatedWorkout);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}