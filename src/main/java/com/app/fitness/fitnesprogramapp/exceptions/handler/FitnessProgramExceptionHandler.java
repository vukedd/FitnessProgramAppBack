package com.app.fitness.fitnesprogramapp.exceptions.handler;

import com.app.fitness.fitnesprogramapp.dtos.ErrorResponseDto;
import com.app.fitness.fitnesprogramapp.exceptions.customExceptions.*;
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

    @ExceptionHandler(VerificationException.class)
    public ResponseEntity<ErrorResponseDto> handleTokenNotFoundException(VerificationException ex) {
        HttpStatus status;
        String errorMessage = "";
        switch (ex.getErrorType()) {
            case TOKEN_NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                errorMessage = "Invalid verification token";
                break;

            case ALREADY_VERIFIED:
                status = HttpStatus.CONFLICT;
                errorMessage = "Account is already verified";
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                errorMessage = "Internal server error";
                break;
        }

        return new ResponseEntity<>(new ErrorResponseDto(errorMessage, ex.getErrorType().name()), status);
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<ErrorResponseDto> handleLoginException(LoginException ex) {
        HttpStatus status;
        String errorMessage = "";
        switch (ex.getErrorType()) {
            case NOT_VERIFIED:
                status = HttpStatus.FORBIDDEN;
                errorMessage = "Account is not verified! A verification link has been sent to your email after the registration!";
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                errorMessage = "Internal server error";
                break;
        }

        return new ResponseEntity<>(new ErrorResponseDto(errorMessage, ex.getErrorType().name()), status);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(ValidationException ex) {
        HttpStatus status;
        String errorMessage = "";
        switch (ex.getErrorType()) {
            case BAD_REQUEST:
                status = HttpStatus.BAD_REQUEST;
                errorMessage = "Invalid token!";
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                errorMessage = "Internal server error";
                break;
        }

        return new ResponseEntity<>(new ErrorResponseDto(errorMessage, ex.getErrorType().name()), status);
    }

    @ExceptionHandler(PasswordChangeException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(PasswordChangeException ex) {
        HttpStatus status;
        String errorMessage = "";
        switch (ex.getErrorType()) {
            case INVALID_CREDENTIALS, WEAK_PASSWORD:
                status = HttpStatus.BAD_REQUEST;
                errorMessage = ex.getMessage();
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                errorMessage = "Internal server error";
                break;
        }

        return new ResponseEntity<>(new ErrorResponseDto(errorMessage, ex.getErrorType().name()), status);

    }

    @ExceptionHandler(ProgramNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleProgramNotFoundException(ProgramNotFoundException ex) {
        return new ResponseEntity<>(new ErrorResponseDto(ex.getMessage(), "NOT_FOUND"), HttpStatus.NOT_FOUND);
    }
}
