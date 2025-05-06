package com.app.fitness.fitnesprogramapp.exceptions.customExceptions;

public class ProgramNotFoundException extends RuntimeException {
    public ProgramNotFoundException(String message) {
        super(message);
    }
}
