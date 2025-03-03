package com.app.fitness.fitnesprogramapp.dtos.program;

import lombok.Data;

import java.util.List;

@Data
public class ProgramCreateDTO {
    private String name;
    private List<WeekDTO> weeks;
}
