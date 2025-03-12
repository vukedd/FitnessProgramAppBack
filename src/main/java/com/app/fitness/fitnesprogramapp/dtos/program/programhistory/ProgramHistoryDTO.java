package com.app.fitness.fitnesprogramapp.dtos.program.programhistory;

import com.app.fitness.fitnesprogramapp.dtos.program.details.WeekDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramHistoryDTO {
    private Long startedProgramId;
    private Long programId;
    private String title;
    private List<StartedWeekHistoryDTO> startedWeeks;
    private List<WeekDetailsDTO> weeks;

}
