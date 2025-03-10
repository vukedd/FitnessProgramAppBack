package com.app.fitness.fitnesprogramapp.dtos.program.currentworkout;

import com.app.fitness.fitnesprogramapp.dtos.program.details.ExerciseDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentWorkoutExerciseDTO {
    private Long id;

    private ExerciseDetailsDTO exercise;

    private List<CurrentSetDTO> sets;

    private int minimumRestTime;
    private int maximumRestTime;

}
