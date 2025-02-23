package com.app.fitness.fitnesprogramapp.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

    @Data
    @AllArgsConstructor
    public class UserRegisterRequestDto {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
    }
