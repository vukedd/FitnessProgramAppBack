package com.app.fitness.fitnesprogramapp.dtos.program.programhistory;

import com.app.fitness.fitnesprogramapp.dtos.program.details.SetDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutExerciseHistoryDTO {
    private Long id;
    private List<SetDetailsDTO> sets;
    private List<DoneSetsHistoryDTO> doneSets;

}
