package com.app.fitness.fitnesprogramapp.exceptions;

import com.app.fitness.fitnesprogramapp.dtos.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static com.app.fitness.fitnesprogramapp.exceptions.LoginException.ErrorType.INVALID_CREDENTIALS;

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
