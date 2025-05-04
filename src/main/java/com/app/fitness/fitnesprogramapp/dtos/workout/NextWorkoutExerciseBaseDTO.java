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
public class NextWorkoutExerciseBaseDTO { // Renamed
    private Long id;
    private String title;
    private String description;
    private String link;
    private List<NextWorkoutExerciseMuscleLinkDTO> exerciseMuscles; // Renamed type
}