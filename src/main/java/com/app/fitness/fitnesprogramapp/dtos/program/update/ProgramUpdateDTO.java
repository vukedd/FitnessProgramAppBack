package com.app.fitness.fitnesprogramapp.dtos.program.update;

import lombok.Data;

import java.util.List;

@Data
public class ProgramUpdateDTO {
    private Long id;
    private String name;
    private List<UpdateWeekDTO> weeks;
}
