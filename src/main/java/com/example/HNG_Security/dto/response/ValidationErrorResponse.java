package com.example.HNG_Security.dto.response;

import com.example.HNG_Security.exception.ValidationError;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {
    private List<ValidationError> errors = new ArrayList<>();

    public void addError(String field, String message) {
        this.errors.add(new ValidationError(field, message));
    }
}
