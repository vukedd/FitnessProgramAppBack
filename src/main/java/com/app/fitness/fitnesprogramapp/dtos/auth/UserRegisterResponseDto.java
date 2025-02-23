package com.app.fitness.fitnesprogramapp.dtos.auth;

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
    private String firstName;
    private String lastName;

    public UserRegisterResponseDto(Long id, String message, String username, String email, String password, String firstName, String lastName) {
        this.id = id;
        this.message = message;
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
