package com.example.HNG_Security.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;


public record ApiError(
        String path,
        String message,
        int statusCode,
        LocalDateTime localDateTime,
        List<ValidationError> errors
) {
}
