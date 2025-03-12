package com.app.fitness.fitnesprogramapp.dtos.program.programhistory;

import com.app.fitness.fitnesprogramapp.dtos.program.details.WorkoutDetailsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartedWeekHistoryDTO {
    private Long id;
    private Long weekId;
    private List<WorkoutDetailsDTO> workouts;
    private List<StartedWorkoutHistoryDTO> startedWorkouts;

    private Date startDate;
    private Date doneDate;
    private boolean finished;
}
