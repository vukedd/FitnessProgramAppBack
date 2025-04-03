package com.app.fitness.fitnesprogramapp.dtos.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutExerciseDefinitionDTO { // Renamed
    private Long id;
    private NextWorkoutExerciseBaseDTO exercise; // Renamed type
    private List<NextWorkoutSetDefinitionDTO> sets; // Renamed type
    private int minimumRestTime;
    private int maximumRestTime;
}