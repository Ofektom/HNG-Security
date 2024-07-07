package com.example.HNG_Security.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AddUserRequest {
    @NotEmpty(message = "User ID is required")
    private String userId;
}
