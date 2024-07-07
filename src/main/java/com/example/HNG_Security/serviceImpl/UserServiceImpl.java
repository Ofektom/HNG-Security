package com.example.HNG_Security.serviceImpl;

import com.example.HNG_Security.dto.request.LoginRequest;
import com.example.HNG_Security.dto.request.RegisterRequest;
import com.example.HNG_Security.dto.response.ApiResponse;
import com.example.HNG_Security.dto.response.ErrorResponse;
import com.example.HNG_Security.dto.response.UserResponse;
import com.example.HNG_Security.exception.EmailAlreadyExistException;
import com.example.HNG_Security.exception.InvalidEmailOrPasswordException;
import com.example.HNG_Security.exception.UserNotFoundException;
import com.example.HNG_Security.model.Organisation;
import com.example.HNG_Security.model.User;
import com.example.HNG_Security.repository.OrganisationRepository;
import com.example.HNG_Security.repository.UserRepository;
import com.example.HNG_Security.service.OrganisationService;
import com.example.HNG_Security.service.UserService;
import com.example.HNG_Security.utils.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganisationService organisationService;
    private final OrganisationRepository organisationRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganisationService organisationService, OrganisationRepository organisationRepository, JwtUtils jwtUtils, @Lazy AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organisationService = organisationService;
        this.organisationRepository = organisationRepository;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not Found"));
    }

    @Override
    @Transactional
    public ResponseEntity<?> registerUser(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException("Email already registered");
        }

        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());

        // Save the user to the database
        User savedUser = userRepository.save(user);

        // Create a new Organisation object with the user's first name appended with "Organisation"
        Organisation organisation = new Organisation();
        organisation.setOrgId(UUID.randomUUID().toString());
        organisation.setName(user.getFirstName() + "'s Organisation");
        organisation.setDescription("Default organisation for " + user.getFirstName());

        Set<User> users = new HashSet<>();
        users.add(savedUser);
        organisation.setUsers(users);

        // Save the organisation to the database
        Organisation savedOrganisation = organisationRepository.save(organisation);

//        Set<Organisation> organisations = new HashSet<>();
//        organisations.add(savedOrganisation);
//
//        savedUser.setOrganisations(organisations);
//        userRepository.save(savedUser);

        UserDetails userDetails = loadUserByUsername(request.getEmail());
        String token = jwtUtils.createJwt.apply(userDetails);

        try {
            UserResponse userResponse = getUserResponse(savedUser);
            ApiResponse.AuthData authData = new ApiResponse.AuthData(token, userResponse);
            ApiResponse<ApiResponse.AuthData> response = new ApiResponse<>("success", "Registration successful", authData);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("Bad request", "Registration unsuccessful", HttpStatus.BAD_REQUEST.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> loginUser(LoginRequest request) {
        UserDetails userDetails = loadUserByUsername(request.getEmail());
        User user = (User) userDetails;

        // Check if the password matches
        if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
            throw new InvalidEmailOrPasswordException("Invalid email or password");
        }


        try{
            String token = jwtUtils.createJwt.apply(userDetails);

            UserResponse userResponse = getUserResponse(user);
            ApiResponse.AuthData authData = new ApiResponse.AuthData(token, userResponse);
            ApiResponse<ApiResponse.AuthData> response = new ApiResponse<>("success", "Login successful", authData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("Bad request", "Authentication failed", HttpStatus.UNAUTHORIZED.value());
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }


    public User getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            log.info("Started in the method");
            log.info(email);

            if (user.getEmail().equals(email) || userBelongsToOrganisation(user, email)) {
                return user;
            } else {
                throw new InvalidEmailOrPasswordException("Access Denied");
            }
        } else {
            throw new UserNotFoundException("User not found");
        }
    }


    public boolean userBelongsToOrganisation(User user, String email) {
        // Get organisations the target user belongs to
        Set<Organisation> targetUserOrganisations = user.getOrganisations();


        // Get organisations the authenticated user belongs to
        List<Organisation> authUserOrganisationsList = organisationService.getUserOrganisations(email);
        Set<Organisation> authUserOrganisations = new HashSet<>(authUserOrganisationsList);

        // Check if there's any common organisation
        return targetUserOrganisations.stream().anyMatch(authUserOrganisations::contains);
    }

    private UserResponse getUserResponse(User user){
        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(user.getUserId());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        return userResponse;
    }
}
