package com.app.fitness.fitnesprogramapp.models.program;

import com.app.fitness.fitnesprogramapp.models.review.Review;
import com.app.fitness.fitnesprogramapp.models.user.User;
import com.app.fitness.fitnesprogramapp.models.week.Week;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Data
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private boolean isPublic;

    @Column(length = 10000)
    private String description;


    @Lob
    @Column(name = "image_data", columnDefinition = "BYTEA") // Ensures DDL is BYTEA
    @JdbcTypeCode(SqlTypes.BINARY) // <<-- ADD THIS (or SqlTypes.BLOB)
    @Basic(fetch = FetchType.LAZY)
    private byte[] imageData;


    @ManyToOne
    private User creator;

    private int followersNumber;

    @OneToMany
    @JoinTable(
            inverseJoinColumns = @JoinColumn(name = "review_id")
    )
    private List<Review> reviews;

    @Transient
    private double rating;

    public double getRating() {
        if (this.reviews != null) {
            return this.reviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);
        }
        return 0.0;
    }

    @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JoinTable(
            inverseJoinColumns = @JoinColumn(name = "week_id")
    )
    private List<Week> weeks;
}
