package com.example.HNG_Security.controller;

import com.example.HNG_Security.dto.request.LoginRequest;
import com.example.HNG_Security.dto.request.RegisterRequest;
import com.example.HNG_Security.dto.request.validation.Errors;
import com.example.HNG_Security.model.UserValidator;
import com.example.HNG_Security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserValidator userValidator;

    public AuthController(
            UserService userService
            ,UserValidator userValidator
    ) {
        this.userService = userService;
        this.userValidator = userValidator;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterRequest request) {
        Errors validationResponse = userValidator.validateRegister(request);
        if (validationResponse != null) {
            return ResponseEntity.unprocessableEntity().body(validationResponse);
        }else {
            return userService.registerUser(request);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Validated @RequestBody LoginRequest request) {
        Errors validationResponse = userValidator.validateLogin(request);
        if (validationResponse != null) {
            return ResponseEntity.unprocessableEntity().body(validationResponse);
        }else {
            return userService.loginUser(request);
        }
    }
}
