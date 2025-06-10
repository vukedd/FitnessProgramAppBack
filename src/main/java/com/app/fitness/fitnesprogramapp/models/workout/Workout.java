package com.app.fitness.fitnesprogramapp.models.workout;

import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "workout_seq")
    @SequenceGenerator(name = "workout_seq", sequenceName = "workout_seq", allocationSize = 50)
    private Long id;

    private String title;
    private String description;

    private Integer position;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            inverseJoinColumns = @JoinColumn(name = "workout_exercise_id")
    )
    private List<WorkoutExercise> workoutExercises;
}
