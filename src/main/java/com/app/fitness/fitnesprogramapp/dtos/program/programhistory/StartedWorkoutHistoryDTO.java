package com.app.fitness.fitnesprogramapp.dtos.program.programhistory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartedWorkoutHistoryDTO {
    private Long id;
    private String title;
    private String description;
    private String number;
    private List<WorkoutExerciseHistoryDTO> workoutExercises;
}
