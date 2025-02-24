package com.app.fitness.fitnesprogramapp.models.review;

import com.app.fitness.fitnesprogramapp.models.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String comment;
    private int rating;
    private boolean deleted;
    private LocalDateTime createdAt;
    @ManyToOne
    @JoinColumn(name = "reviewerId")
    private User reviewer;

}
