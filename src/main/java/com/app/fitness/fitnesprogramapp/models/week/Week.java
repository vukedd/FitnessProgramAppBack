package com.app.fitness.fitnesprogramapp.models.week;

import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Week {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany
    @JoinTable(
            inverseJoinColumns = @JoinColumn(name = "workout_id")
    )
    private List<Workout> workouts;


}
