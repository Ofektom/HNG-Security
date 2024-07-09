package com.example.HNG_Security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DefaultExceptionHandler {
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> UsernameNotFoundException(
            UsernameNotFoundException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<ApiError> EmailIsTakenException(
            EmailAlreadyExistException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidEmailOrPasswordException.class)
    public ResponseEntity<ApiError> InvalidEmailOrPasswordException(
            InvalidEmailOrPasswordException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> UserNotFoundException(
            UserNotFoundException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrganisationNotFoundException.class)
    public ResponseEntity<ApiError> OrganisationNotFoundException(
            OrganisationNotFoundException e) {
        return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }




    private ResponseEntity<ApiError> buildErrorResponse(String message, HttpStatus status) {
        ApiError apiError = new ApiError(
                status.getReasonPhrase(),
                message,
                status.value()
        );
        return new ResponseEntity<>(apiError, status);
    }

}
