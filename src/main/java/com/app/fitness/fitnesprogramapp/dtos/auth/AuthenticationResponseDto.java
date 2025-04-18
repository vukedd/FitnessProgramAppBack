package com.app.fitness.fitnesprogramapp.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponseDto {
    private String token;
    private Long refreshTokenId;
}
