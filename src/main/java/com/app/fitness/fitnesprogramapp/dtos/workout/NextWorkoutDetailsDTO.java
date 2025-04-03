package com.app.fitness.fitnesprogramapp.dtos.workout;

import com.app.fitness.fitnesprogramapp.dtos.workout.NextWorkoutDefinitionDTO; // Renamed
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextWorkoutDetailsDTO { // Renamed
    private Long id;
    private NextWorkoutDefinitionDTO workout; // Renamed type
    private List<NextWorkoutDoneSetDTO> doneSets; // Renamed type
    private Date startDate;
    private Date doneDate;
    private boolean finished;
}