package com.app.fitness.fitnesprogramapp.dtos.program;

import com.app.fitness.fitnesprogramapp.models.program.Program;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramOverviewDTO {
    private Long id;
    private String name;
    private byte[] imageData;
    private String creatorUsername;
    private int followersNumber;
    private Double rating;

    public static ProgramOverviewDTO fromEntity(Program program) {
        return new ProgramOverviewDTO(
                program.getId(),
                program.getName(),
                program.getImageData(),
                program.getCreator().getUsername(),
                program.getFollowersNumber(),
                program.getRating()
        );
    }
}
