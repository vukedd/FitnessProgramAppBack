package com.app.fitness.fitnesprogramapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponseDto {
    private String message;
    private String ErrorType;
}
