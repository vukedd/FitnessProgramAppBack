package com.app.fitness.fitnesprogramapp.exceptions.customExceptions;

import lombok.Getter;

@Getter
public class PasswordChangeException extends RuntimeException {
    private final PasswordChangeException.ErrorType errorType;

    public enum ErrorType {
        INVALID_CREDENTIALS,
        WEAK_PASSWORD,
    }

    public PasswordChangeException(String message, PasswordChangeException.ErrorType errorType) {
        super(message);
        this.errorType = errorType;
    }}
