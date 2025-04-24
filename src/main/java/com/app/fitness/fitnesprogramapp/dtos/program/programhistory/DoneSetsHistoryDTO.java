package com.app.fitness.fitnesprogramapp.dtos.program.programhistory;

import com.app.fitness.fitnesprogramapp.dtos.program.details.SetDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoneSetsHistoryDTO {
    private Long id;

    private SetDetailsDTO setDetails;

    private int volume;

    private double intensity;

    private Date date;

    private Double weightLifted;
}
