package com.app.fitness.fitnesprogramapp.models.program;

import jakarta.persistence.*;
import lombok.Data;

@Embeddable
@Data
public class SetVolume {
    private int minimumVolume;
    private int maximumVolume;
}
