package com.app.fitness.fitnesprogramapp.exceptions.customExceptions;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final ValidationException.ErrorType errorType;

    public enum ErrorType {
        BAD_REQUEST
    }

    public ValidationException(String message, ValidationException.ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
}
