package com.app.fitness.fitnesprogramapp.dtos.volume;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyVolumeReportDTO {
    private LocalDate date;
    private Long sets;
}
