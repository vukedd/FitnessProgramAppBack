package com.app.fitness.fitnesprogramapp.dtos.workout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutDoneSetDTO { // Renamed
    private Long id;
    private NextWorkoutSetDefinitionDTO set; // Renamed type
    private NextWorkoutExerciseDefinitionDTO workoutExercise; // Renamed type
    private int volume;
    private double intensity;
    private Date date;
    private Double weightLifted;
}