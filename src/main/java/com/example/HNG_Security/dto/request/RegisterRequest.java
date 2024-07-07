package com.example.HNG_Security.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotNull
    @Size(min = 1, message = "First name must not be empty")
    private String firstName;

    @NotNull
    @Size(min = 1, message = "Last name must not be empty")
    private String lastName;

    @NotNull
    @Email(message = "Email should be valid")
    private String email;

    @NotNull
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    private String phone;
}
