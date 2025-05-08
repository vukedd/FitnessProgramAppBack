package com.app.fitness.fitnesprogramapp.dtos.program;

import lombok.Data;

import java.util.List;

@Data
public class ProgramCreateDTO {
    private String name;
    private boolean isPublic;
    private String description;
    private List<WeekDTO> weeks;
}
