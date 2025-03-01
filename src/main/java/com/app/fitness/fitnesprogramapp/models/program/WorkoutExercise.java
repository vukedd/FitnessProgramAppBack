package com.app.fitness.fitnesprogramapp.models.program;

import com.app.fitness.fitnesprogramapp.models.exercise.Exercise;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class WorkoutExercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "exercise_id", referencedColumnName = "id")
    private Exercise exercise;

    @OneToMany
    @JoinTable(
            inverseJoinColumns = @JoinColumn(name = "set_id")
    )
    private List<WorkoutExerciseSet> workoutExerciseSets;
}
