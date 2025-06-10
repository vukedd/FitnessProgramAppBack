package com.app.fitness.fitnesprogramapp.models.week;

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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            inverseJoinColumns = @JoinColumn(name = "workout_id")
    )
    private List<Workout> workouts;


}
