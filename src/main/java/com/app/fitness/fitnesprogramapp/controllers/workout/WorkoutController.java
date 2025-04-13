package com.app.fitness.fitnesprogramapp.controllers.workout;

import com.app.fitness.fitnesprogramapp.dtos.exercise.ChangeExerciseOrderDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.CreateSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.program.history.CreateDoneSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.set.AddSetDTO;
import com.app.fitness.fitnesprogramapp.dtos.set.CompleteSetResponseDTO;
import com.app.fitness.fitnesprogramapp.dtos.workout.CompleteWorkoutResponseDTO;
import com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutDTO;
import com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutDoneSetDTO;
import com.app.fitness.fitnesprogramapp.models.set.DoneSet;
import com.app.fitness.fitnesprogramapp.services.workout.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<?> getNextWorkout(@PathVariable Long programId,
                                            Authentication authentication) {
        String username = authentication.getName();
        NextWorkoutDTO nextWorkoutDTO = workoutService.processNextWorkout(programId,username);

        if (nextWorkoutDTO == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(nextWorkoutDTO);
    }

    /**
     * Completes a set in a workout
     * @param doneSetDTO The completed set data
     * @return The updated started workout
     */
    @PostMapping("/completeSet")
    public ResponseEntity<NextWorkoutDoneSetDTO> completeSet(@RequestBody CreateDoneSetDTO doneSetDTO) {
        return ResponseEntity.ok((workoutService.completeSet(doneSetDTO)));
    }

    @PostMapping("/completeWorkout/{workoutId}/{startedProgramId}")
    public ResponseEntity<CompleteWorkoutResponseDTO> completeWorkout(@PathVariable Long workoutId, @PathVariable Long startedProgramId) {
        return ResponseEntity.ok((workoutService.completeWorkout(workoutId,startedProgramId)));
    }

    /**
     * Removes a completed set from a workout
     * @param doneSetId The ID of the completed set to remove
     * @return Response with success message
     */
    @DeleteMapping("/uncompleteSet/{startedWorkoutId}/{doneSetId}")
    public ResponseEntity<CompleteSetResponseDTO> uncompleteSet(@PathVariable Long startedWorkoutId,@PathVariable Long doneSetId) {
        return ResponseEntity.ok(workoutService.uncompleteSet(doneSetId,startedWorkoutId));
    }

    @PutMapping("/addSet/{startedWorkoutId}/{workoutExerciseId}")
    public ResponseEntity<AddSetDTO> addSet(@PathVariable Long startedWorkoutId,@PathVariable Long workoutExerciseId,@RequestBody CreateSetDTO createSetDTO) {
        return ResponseEntity.ok(workoutService.addSet(startedWorkoutId,workoutExerciseId,createSetDTO));
    }

    @PutMapping("/changeExercise/{startedWorkoutId}/{workoutExerciseId}/{newExerciseId}")
    public ResponseEntity<Map<String, Object>> changeExercise(@PathVariable Long startedWorkoutId, @PathVariable Long workoutExerciseId, @PathVariable Long newExerciseId) {
        String result = workoutService.changeExercise(startedWorkoutId, workoutExerciseId, newExerciseId);
        Map<String, Object> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/changeExerciseOrder/{startedWorkoutId}")
    public ResponseEntity<Map<String, Object>> changeExerciseOrder(@PathVariable Long startedWorkoutId, @RequestBody ChangeExerciseOrderDTO changeExerciseOrderDTO) {
        String result = workoutService.changeExerciseOrder(startedWorkoutId, changeExerciseOrderDTO);
        Map<String, Object> response = new HashMap<>();
        response.put("message", result);
        return ResponseEntity.ok(response);
    }
}