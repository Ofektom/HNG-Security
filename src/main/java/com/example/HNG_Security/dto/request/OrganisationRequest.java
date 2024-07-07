package com.example.HNG_Security.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class OrganisationRequest {
    @NotEmpty(message = "Name is required")
    private String name;
    private String description;
}
