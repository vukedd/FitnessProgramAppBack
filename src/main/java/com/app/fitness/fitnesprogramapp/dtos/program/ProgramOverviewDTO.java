package com.app.fitness.fitnesprogramapp.dtos.program;

import com.app.fitness.fitnesprogramapp.models.program.Program;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgramOverviewDTO {
    private Long id;
    private String name;
    private String creatorUsername;
    private int followersNumber;
    private Double rating;
    private Long startedProgramId;
    private boolean finished;

    public static ProgramOverviewDTO fromEntity(Program program) {
        return new ProgramOverviewDTO(
                program.getId(),
                program.getTitle(),
                program.getCreator().getUsername(),
                program.getFollowersNumber(),
                program.getRating(),
                null,
                false
        );
    }
    public static ProgramOverviewDTO fromEntity(Program program, Long startedProgramId, boolean isFinished) {
        if(program==null){
            return new ProgramOverviewDTO(
                    null,
                    "Deleted program",
                    null,
                    0,
                    null,
                    startedProgramId,
                    isFinished
            );
        }
        return new ProgramOverviewDTO(
                program.getId(),
                program.getTitle(),
                program.getCreator().getUsername(),
                program.getFollowersNumber(),
                program.getRating(),
                startedProgramId,
                isFinished
        );
    }

}
