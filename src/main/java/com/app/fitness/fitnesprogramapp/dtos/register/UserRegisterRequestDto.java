package com.app.fitness.fitnesprogramapp.dtos.register;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegisterRequestDto {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
