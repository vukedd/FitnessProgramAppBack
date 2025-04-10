package com.app.fitness.fitnesprogramapp.models.workout;

import com.app.fitness.fitnesprogramapp.models.set.DoneSet;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class StartedWorkout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Workout workout;

    @OneToMany
    private List<DoneSet> doneSets=new ArrayList<>();

    private Date startDate;
    private Date doneDate;

    private LocalTime startTime;
    private LocalTime doneTime;

    private boolean finished;
}
