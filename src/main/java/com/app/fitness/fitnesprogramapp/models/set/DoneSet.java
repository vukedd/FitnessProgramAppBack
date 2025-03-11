package com.app.fitness.fitnesprogramapp.models.set;

import com.app.fitness.fitnesprogramapp.models.exercise.WorkoutExercise;
import com.app.fitness.fitnesprogramapp.models.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Table(name="done_sets")
@Entity
public class DoneSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Set set;

    @ManyToOne
    private WorkoutExercise workoutExercise;

    private int volume;

    private int intensity;

    private Date date;

    private Double weightLifted;
}
