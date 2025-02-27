package com.app.fitness.fitnesprogramapp.exceptions.handler;

import com.app.fitness.fitnesprogramapp.dtos.ErrorResponseDto;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.RegisterException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class FitnessProgramExceptionHandler {

    @ExceptionHandler(RegisterException.class)
    public ResponseEntity<ErrorResponseDto> handleRegisterException(RegisterException ex) {
        HttpStatus status;
        String errorMessage = "";
        switch (ex.getErrorType()) {
            case USERNAME_ALREADY_IN_USE:
                status = HttpStatus.CONFLICT;
                errorMessage = "Username is already in use";
                break;
            case EMAIL_ALREADY_IN_USE:
                status = HttpStatus.CONFLICT;
                errorMessage = "E-mail is already in use";
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                errorMessage = "Internal server error";
                break;
        }

        return new ResponseEntity<>(new ErrorResponseDto(errorMessage, ex.getErrorType().name()), status);
    }
}
