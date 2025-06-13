package com.app.fitness.fitnesprogramapp.models.week;

import com.app.fitness.fitnesprogramapp.models.program.Program;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Week {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "week_seq")
    @SequenceGenerator(name = "week_seq", sequenceName = "week_seq", allocationSize = 50)
    private Long id;
    private Integer position;

    @OneToMany(
            mappedBy = "week",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Workout> workouts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id") // This column will be created in the 'week' table
    private Program program;


}
