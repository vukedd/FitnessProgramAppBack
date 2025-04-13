package com.app.fitness.fitnesprogramapp.dtos.exercise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeExerciseOrderDTO {
    private List<Long> workoutExerciseIds;
}
