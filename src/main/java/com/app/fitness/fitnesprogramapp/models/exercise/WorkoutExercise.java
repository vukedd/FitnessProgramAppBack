package com.app.fitness.fitnesprogramapp.models.exercise;

import com.app.fitness.fitnesprogramapp.models.set.Set;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class WorkoutExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer position;

    @ManyToOne
    @JoinColumn(name = "exercise_id", referencedColumnName = "id")
    private Exercise exercise;

    @OneToMany
    @JoinTable(
            inverseJoinColumns = @JoinColumn(name = "set_id")
    )
    private List<Set> sets;

    private int minimumRestTime;
    private int maximumRestTime;
}
