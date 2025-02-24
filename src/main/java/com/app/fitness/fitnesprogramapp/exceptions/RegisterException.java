package com.app.fitness.fitnesprogramapp.exceptions;

import lombok.Getter;

@Getter
public class RegisterException extends RuntimeException{
    private final RegisterException.ErrorType errorType;

    public enum ErrorType {
        USERNAME_ALREADY_IN_USE,
        EMAIL_ALREADY_IN_USE
    }

    public RegisterException(String message, RegisterException.ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
}
