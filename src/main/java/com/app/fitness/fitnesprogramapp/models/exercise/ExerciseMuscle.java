package com.app.fitness.fitnesprogramapp.models.exercise;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ExerciseMuscle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @ManyToOne
    @JoinColumn(name = "muscle_id")
    private Muscle muscle;

    @Column(name = "intensity")
    private Integer intensity; // You could use an enum, int, or other type as needed

}
