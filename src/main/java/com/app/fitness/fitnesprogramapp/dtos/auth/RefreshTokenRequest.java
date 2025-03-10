package com.app.fitness.fitnesprogramapp.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenRequest {
    Long refreshTokenId;
}