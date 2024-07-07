package com.example.HNG_Security.service;

import com.example.HNG_Security.dto.request.OrganisationRequest;
import com.example.HNG_Security.model.Organisation;

import java.util.List;

public interface OrganisationService {
    List<Organisation> getUserOrganisations(String email);

    Organisation getOrganisationById(String orgId);

    Organisation createOrganisation(OrganisationRequest request, String email);

    void addUserToOrganisation(String orgId, String userId);
}
