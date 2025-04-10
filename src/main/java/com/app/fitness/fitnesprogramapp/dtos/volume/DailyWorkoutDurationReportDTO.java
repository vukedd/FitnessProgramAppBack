package com.app.fitness.fitnesprogramapp.dtos.volume;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Data
public class DailyWorkoutDurationReportDTO {
    private float duration;
    private LocalDate date;
}
