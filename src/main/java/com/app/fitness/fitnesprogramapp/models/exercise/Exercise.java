package com.app.fitness.fitnesprogramapp.models.exercise;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseMuscle> exerciseMuscles = new ArrayList<>();

    // Method to add a muscle with intensity
    public void addMuscle(Muscle muscle, Integer intensity) {
        ExerciseMuscle exerciseMuscle = new ExerciseMuscle();
        exerciseMuscle.setExercise(this);
        exerciseMuscle.setMuscle(muscle);
        exerciseMuscle.setIntensity(intensity);
        exerciseMuscles.add(exerciseMuscle);
    }
}
