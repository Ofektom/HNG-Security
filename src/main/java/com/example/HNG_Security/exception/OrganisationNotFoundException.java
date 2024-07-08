package com.example.HNG_Security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class OrganisationNotFoundException extends RuntimeException{
    public OrganisationNotFoundException(String message) {
        super(message);
    }
}
