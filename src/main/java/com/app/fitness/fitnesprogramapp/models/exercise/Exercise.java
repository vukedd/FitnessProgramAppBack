package com.app.fitness.fitnesprogramapp.models.exercise;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;

    @OneToMany
    @JoinTable(
            inverseJoinColumns = @JoinColumn(name = "muscle_id")
    )
    private List<Muscle> workedMuscles;
}
