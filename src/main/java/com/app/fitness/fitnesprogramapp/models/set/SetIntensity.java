package com.app.fitness.fitnesprogramapp.models.set;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetIntensity {
    private int minimumIntensity;
    private int maximumIntensity;
}
