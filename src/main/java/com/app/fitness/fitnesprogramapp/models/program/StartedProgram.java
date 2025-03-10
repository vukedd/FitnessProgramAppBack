package com.app.fitness.fitnesprogramapp.models.program;

import com.app.fitness.fitnesprogramapp.models.week.StartedWeek;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class StartedProgram {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Program program;

    @OneToMany
    private List<StartedWeek> startedWeeks=new ArrayList<>();

    private Date startDate;
    private Date doneDate;
    private boolean finished;
}
