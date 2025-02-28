package com.app.fitness.fitnesprogramapp.models.program;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class SetIntensity {
    private int minimumIntensity;
    private int maximumIntensity;
}
