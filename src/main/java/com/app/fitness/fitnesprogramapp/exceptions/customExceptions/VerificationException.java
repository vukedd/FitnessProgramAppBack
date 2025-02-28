package com.app.fitness.fitnesprogramapp.exceptions.customExceptions;

import lombok.Getter;

@Getter
public class VerificationException extends RuntimeException{
    private final VerificationException.ErrorType errorType;

    public enum ErrorType {
        TOKEN_NOT_FOUND,
        ALREADY_VERIFIED
    }

    public VerificationException(String message, VerificationException.ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }
}
