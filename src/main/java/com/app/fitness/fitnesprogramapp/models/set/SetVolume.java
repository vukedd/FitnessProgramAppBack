package com.app.fitness.fitnesprogramapp.models.set;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetVolume {
    private int minimumVolume;
    private int maximumVolume;
}
