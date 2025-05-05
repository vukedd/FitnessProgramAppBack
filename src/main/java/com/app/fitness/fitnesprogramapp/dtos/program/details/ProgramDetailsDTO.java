package com.app.fitness.fitnesprogramapp.dtos.program.details;

import com.app.fitness.fitnesprogramapp.dtos.user.UserProgramDetailsDTO;
import lombok.Data;

import java.util.List;

@Data
public class ProgramDetailsDTO {
    private Long id;

    private boolean isCreatedByUser;

    private String name;

    private UserProgramDetailsDTO creator;

    private int followersNumber;

    private double rating;

    private List<WeekDetailsDTO> weeks;


}
