package com.app.fitness.fitnesprogramapp.dtos.program.update;

import lombok.Data;

import java.util.List;

@Data
public class UpdateWeekDTO {
    private int id;
    private List<UpdateWorkoutDTO> workouts;
}
