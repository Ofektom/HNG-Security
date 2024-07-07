package com.example.HNG_Security.controller;

import com.example.HNG_Security.dto.request.AddUserRequest;
import com.example.HNG_Security.dto.request.OrganisationRequest;
import com.example.HNG_Security.dto.response.ApiResponse;
import com.example.HNG_Security.dto.response.OrganisationListResponse;
import com.example.HNG_Security.dto.response.OrganisationResponse;
import com.example.HNG_Security.model.Organisation;
import com.example.HNG_Security.service.OrganisationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organisations")
public class OrganisationController {

    private final OrganisationService organisationService;

    public OrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<OrganisationListResponse>> getUserOrganisations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // Assuming email is the unique identifier

        List<Organisation> organisations = organisationService.getUserOrganisations(email);

        List<OrganisationResponse> organisationsResponse = organisations.stream()
                .map(this::mapToOrganisationResponse)
                .collect(Collectors.toList());

        OrganisationListResponse organisationListResponse = new OrganisationListResponse();
        organisationListResponse.setOrganisations(organisationsResponse);


        ApiResponse<OrganisationListResponse> response = new ApiResponse<>("success", "User organisations retrieved successfully", organisationListResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orgId}")
    public ResponseEntity<ApiResponse<OrganisationResponse>> getOrganisationById(@PathVariable String orgId) {
        Organisation organisation = organisationService.getOrganisationById(orgId);
        OrganisationResponse organisationResponse = mapToOrganisationResponse(organisation);

        ApiResponse<OrganisationResponse> response = new ApiResponse<>("success", "Organisation retrieved successfully", organisationResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrganisationResponse>> createOrganisation(@Valid @RequestBody OrganisationRequest request) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Organisation organisation = organisationService.createOrganisation(request, email);
            OrganisationResponse organisationResponse = mapToOrganisationResponse(organisation);

            ApiResponse<OrganisationResponse> response = new ApiResponse<>("success", "Organisation created successfully", organisationResponse);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ApiResponse<OrganisationResponse> errorResponse = new ApiResponse<>("Bad request", "Client error", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{orgId}/users")
    public ResponseEntity<ApiResponse<String>> addUserToOrganisation(@PathVariable String orgId, @Valid @RequestBody AddUserRequest request) {
        organisationService.addUserToOrganisation(orgId, request.getUserId());

        ApiResponse<String> response = new ApiResponse<>("success", "User added to organisation successfully", null);
        return ResponseEntity.ok(response);
    }

    private OrganisationResponse mapToOrganisationResponse(Organisation organisation) {
        OrganisationResponse response = new OrganisationResponse();
        response.setOrgId(organisation.getOrgId());
        response.setName(organisation.getName());
        response.setDescription(organisation.getDescription());
        return response;
    }
}
