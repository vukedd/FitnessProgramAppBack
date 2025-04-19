package com.app.fitness.fitnesprogramapp.models.workout;

import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String number;

    private Integer position;

    @OneToMany
    @JoinTable(
            inverseJoinColumns = @JoinColumn(name = "workout_exercise_id")
    )
    private List<WorkoutExercise> workoutExercises;
}
