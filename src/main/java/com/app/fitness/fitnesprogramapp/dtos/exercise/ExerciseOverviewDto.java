package com.app.fitness.fitnesprogramapp.dtos.exercise;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExerciseOverviewDto {
    private Long id;
    private String title;
    private String link;
}
