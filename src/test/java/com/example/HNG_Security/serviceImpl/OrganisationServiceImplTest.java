package com.example.HNG_Security.serviceImpl;

import com.example.HNG_Security.dto.request.OrganisationRequest;
import com.example.HNG_Security.exception.OrganisationNotFoundException;
import com.example.HNG_Security.exception.UserNotFoundException;
import com.example.HNG_Security.model.Organisation;
import com.example.HNG_Security.model.User;
import com.example.HNG_Security.repository.OrganisationRepository;
import com.example.HNG_Security.repository.UserRepository;
import com.example.HNG_Security.serviceImpl.OrganisationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class OrganisationServiceImplTest {

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganisationServiceImpl organisationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testGetUserOrganisations_UserNotFound() {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            organisationService.getUserOrganisations(email);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetOrganisationById_OrganisationExists() {
        String orgId = "org123";
        Organisation organisation = new Organisation();
        organisation.setOrgId(orgId);

        when(organisationRepository.findByOrgId(anyString())).thenReturn(Optional.of(organisation));

        Organisation result = organisationService.getOrganisationById(orgId);

        assertNotNull(result);
        assertEquals(orgId, result.getOrgId());
    }

    @Test
    void testGetOrganisationById_OrganisationNotFound() {
        String orgId = "nonexistentOrgId";

        when(organisationRepository.findByOrgId(anyString())).thenReturn(Optional.empty());

        OrganisationNotFoundException exception = assertThrows(OrganisationNotFoundException.class, () -> {
            organisationService.getOrganisationById(orgId);
        });

        assertEquals("Organisation not found", exception.getMessage());
    }

    @Test
    void testCreateOrganisation_UserExists() {
        String userEmail = "john.doe@example.com";
        User user = new User();
        user.setEmail(userEmail);

        OrganisationRequest request = new OrganisationRequest();
        request.setName("New Organisation");
        request.setDescription("This is a new organisation");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(organisationRepository.save(any(Organisation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Organisation createdOrganisation = organisationService.createOrganisation(request, userEmail);

        assertNotNull(createdOrganisation);
        assertEquals(request.getName(), createdOrganisation.getName());
        assertEquals(request.getDescription(), createdOrganisation.getDescription());
        assertTrue(createdOrganisation.getUsers().contains(user));
    }

    @Test
    void testCreateOrganisation_UserNotFound() {
        String userEmail = "nonexistent@example.com";
        OrganisationRequest request = new OrganisationRequest();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            organisationService.createOrganisation(request, userEmail);
        });

        assertEquals("User not found", exception.getMessage());
    }
}