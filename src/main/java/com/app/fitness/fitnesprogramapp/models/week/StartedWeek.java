package com.app.fitness.fitnesprogramapp.models.week;

import com.app.fitness.fitnesprogramapp.models.workout.StartedWorkout;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class StartedWeek {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long weekId;

    @OneToMany
    private List<StartedWorkout> startedWorkouts=new ArrayList<>();

    private Date startDate;
    private Date doneDate;
    private boolean finished;
}
