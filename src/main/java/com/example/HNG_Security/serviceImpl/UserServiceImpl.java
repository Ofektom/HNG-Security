package com.example.HNG_Security.serviceImpl;

import com.example.HNG_Security.dto.request.LoginRequest;
import com.example.HNG_Security.dto.request.RegisterRequest;
import com.example.HNG_Security.dto.request.validation.Errors;
import com.example.HNG_Security.dto.response.ApiResponse;
import com.example.HNG_Security.dto.response.ErrorResponse;
import com.example.HNG_Security.dto.response.UserResponse;
import com.example.HNG_Security.exception.EmailAlreadyExistException;
import com.example.HNG_Security.exception.InvalidEmailOrPasswordException;
import com.example.HNG_Security.exception.UserNotFoundException;
import com.example.HNG_Security.model.Organisation;
import com.example.HNG_Security.model.User;
import com.example.HNG_Security.model.UserValidator;
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
    private final UserValidator userValidator;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganisationService organisationService, OrganisationRepository organisationRepository, JwtUtils jwtUtils, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organisationService = organisationService;
        this.organisationRepository = organisationRepository;
        this.jwtUtils = jwtUtils;
        this.userValidator = userValidator;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not Found"));
    }


    @Override
    @Transactional
    public ResponseEntity<?> registerUser(RegisterRequest request) {
        log.info("Starting registration process for: {}", request.getEmail());

        Errors validationResponse = userValidator.validateUser(request);
        if (validationResponse != null) {
            log.error("Validation errors: {}", validationResponse.getErrors());
            return ResponseEntity.badRequest().body(validationResponse.getErrors());
        }

        try {
            User user = new User();
            user.setUserId(UUID.randomUUID().toString());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPhone(request.getPhone());

            log.info("Saving user: {}", user);
            User savedUser = userRepository.save(user);

            Organisation organisation = new Organisation();
            organisation.setOrgId(UUID.randomUUID().toString());
            organisation.setName(user.getFirstName() + "'s Organisation");
            organisation.setDescription("Default organisation for " + user.getFirstName());
            organisation.setUsers(new HashSet<>(Collections.singletonList(savedUser)));

            log.info("Saving organisation: {}", organisation);
            Organisation savedOrganisation = organisationRepository.save(organisation);

            // Now set the organisation to the user and update the user
            savedUser.setOrganisations(new HashSet<>(Collections.singletonList(savedOrganisation)));
            userRepository.save(savedUser);

            // Generate JWT token
            UserDetails userDetails = loadUserByUsername(request.getEmail());
            String token = jwtUtils.createJwt.apply(userDetails);

            UserResponse userResponse = getUserResponse(savedUser);
            ApiResponse.AuthData authData = new ApiResponse.AuthData(token, userResponse);
            ApiResponse<ApiResponse.AuthData> response = new ApiResponse<>("success", "Registration successful", authData);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Exception occurred during registration: ", e);
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


    public User getUserById(String id) {
        Optional<User> optionalUser = userRepository.findByUserId(id);
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
        Set<Organisation> targetUserOrganisations = user.getOrganisations();

        List<Organisation> authUserOrganisationsList = organisationService.getUserOrganisations(email);
        Set<Organisation> authUserOrganisations = new HashSet<>(authUserOrganisationsList);

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
