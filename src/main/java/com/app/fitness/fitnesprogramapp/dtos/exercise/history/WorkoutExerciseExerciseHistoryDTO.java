package com.app.fitness.fitnesprogramapp.dtos.exercise.history;

import com.app.fitness.fitnesprogramapp.dtos.program.programhistory.DoneSetsHistoryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutExerciseExerciseHistoryDTO {
    private Long id;
    private List<DoneSetsHistoryDTO> doneSets;
}
