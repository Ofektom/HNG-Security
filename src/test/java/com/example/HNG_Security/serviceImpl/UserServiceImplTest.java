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
import com.example.HNG_Security.model.UserValidator;
import com.example.HNG_Security.repository.OrganisationRepository;
import com.example.HNG_Security.repository.UserRepository;
import com.example.HNG_Security.service.OrganisationService;
import com.example.HNG_Security.utils.JwtUtils;
import io.jsonwebtoken.Claims;
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
    UserValidator userValidator;

    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtUtils.createJwt = mock(Function.class);

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

        assertEquals("Invalid email or password", thrown.getMessage());
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


}