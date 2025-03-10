package com.app.fitness.fitnesprogramapp.dtos.program.history;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CreateExerciseHistoryDTO {
    List<CreateDoneSetDTO> doneSets;
}
