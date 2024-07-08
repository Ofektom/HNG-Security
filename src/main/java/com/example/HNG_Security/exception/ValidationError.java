package com.example.HNG_Security.exception;


public record ValidationError(
        String field,
        String message
) {
}