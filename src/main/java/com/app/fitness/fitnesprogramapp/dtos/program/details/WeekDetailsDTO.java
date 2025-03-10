package com.app.fitness.fitnesprogramapp.dtos.program.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeekDetailsDTO {
    private Long id;
    private List<WorkoutDetailsDTO> workouts;
}
