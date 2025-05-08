package com.app.fitness.fitnesprogramapp.dtos.program.update;

import lombok.Data;

import java.util.List;

@Data
public class ProgramUpdateDTO {
    private Long id;
    private String name;
    private boolean isPublic;
    private String description;
    private List<UpdateWeekDTO> weeks;
}
