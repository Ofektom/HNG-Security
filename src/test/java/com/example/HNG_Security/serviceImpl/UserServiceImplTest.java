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
import com.example.HNG_Security.serviceImpl.UserServiceImpl;
import com.example.HNG_Security.utils.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import static org.mockito.Mockito.*;


class UserServiceImplTest {

    @InjectMocks
    UserServiceImpl userService;

    @InjectMocks
    UserValidator userValidator;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    OrganisationService organisationService;

    @Mock
    OrganisationRepository organisationRepository;

    @Mock
    JwtUtils jwtUtils;

    @Mock
    AuthenticationManager authenticationManager;

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtils.createJwt = mock(Function.class);

    }


    @Test
    void testRegisterUser_Successful_DefaultOrganisation() {
        RegisterRequest signupRequest = new RegisterRequest("John", "Doe", "john.doe@example.com", "password", "1234567890");

        User user = new User();
        user.setUserId("someUserId");
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(signupRequest.getPassword());
        user.setPhone(signupRequest.getPhone());

        Organisation organisation = new Organisation();
        organisation.setOrgId("someOrgId");
        organisation.setName(user.getFirstName() + "'s Organisation");
        organisation.setDescription("Default organisation for " + user.getFirstName());
        organisation.setUsers(new HashSet<>(Collections.singletonList(user)));

        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(new User()));
        when(jwtUtils.createJwt.apply(any(User.class))).thenReturn("someToken");
        when(organisationRepository.save(any(Organisation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> responseEntity = userService.registerUser(signupRequest);
        assertNotNull(responseEntity);
        assertNotNull(responseEntity);
        ApiResponse<?> response = (ApiResponse<?>) responseEntity.getBody();
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals("success", response.getStatus());
        assertEquals("Registration successful", response.getMessage());
        assertNotNull(response.getData());

        ApiResponse.AuthData authData =(ApiResponse.AuthData) response.getData();
        assertNotNull(authData);
        assertEquals("someToken", authData.getAccessToken());

        UserResponse userResponse = authData.getUser();
        assertNotNull(userResponse);
        assertEquals(user.getFirstName(), userResponse.getFirstName());
        assertEquals(user.getLastName(), userResponse.getLastName());
        assertEquals(user.getEmail(), userResponse.getEmail());
        assertNotNull(userResponse.getPhone());

    }


    @Test
    public void testDefaultOrganisationName() {

        User user = new User();
        user.setFirstName("John");
        user.setEmail("john.doe@example.com");

        // Arrange
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        User createdUser = userRepository.save(user);
        Optional<User> createdUserCheck = userRepository.findByEmail(user.getEmail());

        if (createdUserCheck.isEmpty()) {
            fail("User not found after save");
        }

        Organisation organisation = new Organisation();
        String orgName = createdUser.getFirstName() + "'s Organisation";
        organisation.setName(orgName);

        Set<User> users = new HashSet<>();
        users.add(createdUser);
        organisation.setUsers(users);

        // Assert
        assertNotNull(organisation, "Organisation should not be null");
        assertEquals("John's Organisation", organisation.getName(), "Organisation name is not correct");
        assertEquals(1, organisation.getUsers().size(), "Organisation should have one user");
        assertTrue(organisation.getUsers().contains(createdUser), "Organisation should contain the created user");

        // Verify repository interactions
        verify(userRepository, times(1)).save(user);
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    void testLoginUser_Successful() {
        // Given
        String email = "test@example.com";
        String password = "password";
        String encodedPassword = "$2a$10$DowV8ZoYwHh8hHJ4Izxz.u1F2aOT8u5a4BbEwZEBhmy9JshSZ/tGO"; // bcrypt hash for "password"
        String token = "someToken";

        LoginRequest loginRequest = new LoginRequest(email, password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        // Mock the dependencies
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtils.createJwt.apply(user)).thenReturn(token);

        // When
        ResponseEntity<?> responseEntity = userService.loginUser(loginRequest);

        // Then
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        ApiResponse<?> response = (ApiResponse<?>) responseEntity.getBody();
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Login successful", response.getMessage());

        ApiResponse.AuthData authData = (ApiResponse.AuthData) response.getData();
        assertNotNull(authData);
        assertEquals(token, authData.getAccessToken());

        UserResponse userResponse = authData.getUser();
        assertNotNull(userResponse);
        assertEquals(email, userResponse.getEmail());
    }

    // Test case for login failure due to incorrect password
    @Test
    void testLoginUser_Failure_IncorrectPassword() {
        // Given
        String email = "test@example.com";
        String password = "password";
        String incorrectPassword = "wrongPassword";
        String encodedPassword = "$2a$10$DowV8ZoYwHh8hHJ4Izxz.u1F2aOT8u5a4BbEwZEBhmy9JshSZ/tGO"; // bcrypt hash for "password"

        LoginRequest loginRequest = new LoginRequest(email, incorrectPassword);

        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        // Mock the dependencies
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(incorrectPassword, encodedPassword)).thenReturn(false);

        // When & Then
        InvalidEmailOrPasswordException thrown = assertThrows(
                InvalidEmailOrPasswordException.class,
                () -> userService.loginUser(loginRequest)
        );

        assertEquals("Authentication failed", thrown.getMessage());
    }

    // Test case for login failure due to user not found
    @Test
    void testLoginUser_Failure_UserNotFound() {
        // Given
        String email = "notfound@example.com";
        String password = "password";

        LoginRequest loginRequest = new LoginRequest(email, password);

        // Mock the dependencies
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(email)
        );
    }


    @Test
    void testCreateJwtToken() {
        User user = new User();
        user.setUserId("someUserId");

        when(jwtUtils.createJwt.apply(user)).thenReturn("generatedToken");

        String token = jwtUtils.createJwt.apply(user);

        assertEquals("generatedToken", token);
    }





    @Test
    void testRegisterUser_DatabaseConstraintViolation() {
        RegisterRequest signupRequest = new RegisterRequest("John", "Doe", "john.doe@example.com", "password", "1234567890");

        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database constraint violation"));

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> userService.registerUser(signupRequest)
        );

        assertEquals("Database constraint violation", thrown.getMessage());
    }

    @Test
    void testValidateRegister_Successful() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john.doe@example.com", "password", "1234567890");

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);

        Errors errors = userValidator.validateRegister(request);

        assertNull(errors, "Expected no validation errors");
    }

    @Test
    void testValidateRegister_ValidationErrors() {
        RegisterRequest request = new RegisterRequest("", "", "invalid-email", "", "");

        when(userRepository.existsByEmail("invalid-email")).thenReturn(false);

        Errors errors = userValidator.validateRegister(request);

        assertNotNull(errors);
        assertEquals(4, errors.getErrors().size());
        assertTrue(errors.getErrors().stream().anyMatch(e -> e.getField().equals("firstName") && e.getMessage().equals("First name must not be null or empty")));
        assertTrue(errors.getErrors().stream().anyMatch(e -> e.getField().equals("lastName") && e.getMessage().equals("Last name must not be null or empty")));
        assertTrue(errors.getErrors().stream().anyMatch(e -> e.getField().equals("email") && e.getMessage().equals("Email is in an incorrect format")));
        assertTrue(errors.getErrors().stream().anyMatch(e -> e.getField().equals("password") && e.getMessage().equals("Password must not be null or empty")));
    }

    @Test
    void testValidateRegister_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("John", "Doe", "john.doe@example.com", "password", "1234567890");

        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        Errors errors = userValidator.validateRegister(request);

        assertNotNull(errors);
        assertEquals(1, errors.getErrors().size());
        assertTrue(errors.getErrors().stream().anyMatch(e -> e.getField().equals("email") && e.getMessage().equals("Email already exists")));
    }

    @Test
    void testValidateLogin_Successful() {
        LoginRequest request = new LoginRequest("john.doe@example.com", "password");

        Errors errors = userValidator.validateLogin(request);

        assertNull(errors, "Expected no validation errors");
    }

    @Test
    void testValidateLogin_ValidationErrors() {
        LoginRequest request = new LoginRequest("", "");

        Errors errors = userValidator.validateLogin(request);

        assertNotNull(errors);
        assertEquals(2, errors.getErrors().size());
        assertTrue(errors.getErrors().stream().anyMatch(e -> e.getField().equals("email") && e.getMessage().equals("Email must not be null or empty")));
        assertTrue(errors.getErrors().stream().anyMatch(e -> e.getField().equals("password") && e.getMessage().equals("Password must not be null or empty")));
    }


}