package com.app.fitness.fitnesprogramapp.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfileDto{
    private String username;
    private String email;
    private String firstName;
    private String lastName;
}
