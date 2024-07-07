package com.example.HNG_Security.serviceImpl;

import com.example.HNG_Security.dto.request.OrganisationRequest;
import com.example.HNG_Security.exception.OrganisationNotFoundException;
import com.example.HNG_Security.exception.UserNotFoundException;
import com.example.HNG_Security.model.Organisation;
import com.example.HNG_Security.model.User;
import com.example.HNG_Security.repository.OrganisationRepository;
import com.example.HNG_Security.repository.UserRepository;
import com.example.HNG_Security.service.OrganisationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class OrganisationServiceImpl implements OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final UserRepository userRepository;

    public OrganisationServiceImpl(OrganisationRepository organisationRepository, UserRepository userRepository) {
        this.organisationRepository = organisationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Organisation> getUserOrganisations(String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        User user = userOptional.get();
        return organisationRepository.findByUsers(user);
    }

    @Transactional(readOnly = true)
    public Organisation getOrganisationById(String orgId) {
        return organisationRepository.findByOrgId(orgId)
                .orElseThrow(() -> new OrganisationNotFoundException("Organisation not found"));
    }

    @Transactional
    public Organisation createOrganisation(OrganisationRequest request, String userEmail) {
        Optional<User> userOptional = userRepository.findByEmail(userEmail);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        User user = userOptional.get();

        Organisation organisation = new Organisation();
        organisation.setOrgId(UUID.randomUUID().toString());
        organisation.setName(request.getName());
        organisation.setDescription(request.getDescription());
        organisation.setUsers(Set.of(user));

        return organisationRepository.save(organisation);
    }

    @Transactional
    public void addUserToOrganisation(String orgId, String userId) {
        Organisation organisation = organisationRepository.findByOrgId(orgId)
                .orElseThrow(() -> new OrganisationNotFoundException("Organisation not found"));

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        organisation.getUsers().add(user);
        organisationRepository.save(organisation);
    }
}
