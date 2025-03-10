package com.app.fitness.fitnesprogramapp.dtos.program.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetVolumeDetailsDTO {
    private int minimumVolume;
    private int maximumVolume;
}
