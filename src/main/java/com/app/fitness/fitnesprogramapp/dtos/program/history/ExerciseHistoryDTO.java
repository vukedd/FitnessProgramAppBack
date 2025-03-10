package com.app.fitness.fitnesprogramapp.dtos.program.history;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ExerciseHistoryDTO {
    private int id;
    private String exerciseTitle;
    private List<DoneSetDTO> doneSets;
}
