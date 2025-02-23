package com.app.fitness.fitnesprogramapp.models.program;

import com.app.fitness.fitnesprogramapp.models.user.User;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    @Column(length = 1000000)
    private byte[] imageData;

    @ManyToOne
    private User creator;
}
