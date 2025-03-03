package com.app.fitness.fitnesprogramapp.dtos.program;

import lombok.Data;

import java.util.List;

@Data
public class WeekDTO {
    private List<WorkoutDTO> workouts;
}
