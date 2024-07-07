package com.example.HNG_Security.service;

import com.example.HNG_Security.dto.request.LoginRequest;
import com.example.HNG_Security.dto.request.RegisterRequest;
import com.example.HNG_Security.dto.response.UserResponse;
import com.example.HNG_Security.model.User;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface UserService {
    ResponseEntity<?> registerUser(RegisterRequest request);

    ResponseEntity<?> loginUser(LoginRequest request);


    User getUserById(Long id);
}
