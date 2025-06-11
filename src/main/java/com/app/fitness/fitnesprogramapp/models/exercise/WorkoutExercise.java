package com.app.fitness.fitnesprogramapp.models.exercise;

import com.app.fitness.fitnesprogramapp.models.set.Set;
import com.app.fitness.fitnesprogramapp.models.workout.Workout;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class WorkoutExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workout_exercise_seq")
    @SequenceGenerator(name = "workout_exercise_seq", sequenceName = "workout_exercise_seq", allocationSize = 50)
    private Long id;

    private Integer position;

    @ManyToOne
    @JoinColumn(name = "exercise_id", referencedColumnName = "id")
    private Exercise exercise;

    @OneToMany(
            mappedBy = "workoutExercise",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Set> sets;

    private int minimumRestTime;
    private int maximumRestTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id")
    private Workout workout;
}
