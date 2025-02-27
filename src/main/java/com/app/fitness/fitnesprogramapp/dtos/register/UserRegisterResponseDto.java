package com.app.fitness.fitnesprogramapp.dtos.register;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRegisterResponseDto {
    private Long id;
    private String message;
    private String username;
    private String email;
    private String password;

    public UserRegisterResponseDto(Long id, String message, String username, String email, String password) {
        this.id = id;
        this.message = message;
        this.username = username;
        this.email = email;
        this.password = password;

    }
}
