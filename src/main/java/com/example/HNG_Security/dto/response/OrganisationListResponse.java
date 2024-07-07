package com.example.HNG_Security.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class OrganisationListResponse {
    private List<OrganisationResponse> organisations;
}
