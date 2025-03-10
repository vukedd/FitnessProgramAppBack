package com.app.fitness.fitnesprogramapp.dtos.program.startprogram;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartProgramResponseDTO {
    private Long startedProgramId;
    private String programTitle;
}
