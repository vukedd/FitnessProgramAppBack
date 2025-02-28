package com.app.fitness.fitnesprogramapp.exceptions.customExceptions;

import lombok.Getter;

@Getter
public class LoginException extends RuntimeException {
    private final LoginException.ErrorType errorType;

    public enum ErrorType {
        NOT_VERIFIED
    }

    public LoginException(String message, LoginException.ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
}
