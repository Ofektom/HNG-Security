package com.example.HNG_Security.repository;

import com.example.HNG_Security.model.Organisation;
import com.example.HNG_Security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganisationRepository extends JpaRepository<Organisation, String> {
    List<Organisation> findByUsers(User user);

    Optional<Organisation> findByOrgId(String orgId);

    Organisation findByUsersContains(User user1);


}
