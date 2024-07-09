package com.example.HNG_Security.model;

import com.example.HNG_Security.dto.request.LoginRequest;
import com.example.HNG_Security.dto.request.RegisterRequest;
import com.example.HNG_Security.dto.request.validation.Errors;
import com.example.HNG_Security.dto.request.validation.Error;
import com.example.HNG_Security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class UserValidator {
    private final UserRepository userRepository;

    @Autowired
    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Errors validateRegister(RegisterRequest registerRequest) {
        Errors errors = new Errors();
        List<Error> errorList = new ArrayList<>();

        String firstName = registerRequest.getFirstName();
        if (firstName == null || firstName.isEmpty()) {
            errorList.add(new Error("firstName", "First name must not be null or empty"));
        } else if (!isString(firstName)) {
            errorList.add(new Error("firstName", "First name must be a string"));
        }


        String lastName = registerRequest.getLastName();
        if (lastName == null || lastName.isEmpty()) {
            errorList.add(new Error("lastName", "Last name must not be null or empty"));
        } else if (!isString(lastName)) {
            errorList.add(new Error("lastName", "Last name must be a string"));
        }


        String email = registerRequest.getEmail();
        if (email == null || email.isEmpty()) {
            errorList.add(new Error("email", "Email must not be null or empty"));
        } else if (!isString(email)) {
            errorList.add(new Error("email", "Email must be a string"));
        } else if (userRepository.existsByEmail(email)) {
            errorList.add(new Error("email", "Email already exists"));
        } else if (!isValidEmail(email)) {
            errorList.add(new Error("email", "Email is in an incorrect format"));
        }


        String password = registerRequest.getPassword();
        if (password == null || password.isEmpty()) {
            errorList.add(new Error("password", "Password must not be null or empty"));
        } else if (!isString(password)) {
            errorList.add(new Error("password", "Password must be a string"));
        }

        if (!errorList.isEmpty()) {
            errors.setErrors(errorList);
            return errors;
        }


        return null;
    }

    public Errors validateLogin(LoginRequest loginRequest) {
        Errors errors = new Errors();
        List<Error> errorList = new ArrayList<>();

        String email = loginRequest.getEmail();
        if (email == null || email.isEmpty()) {
            errorList.add(new Error("email", "Email must not be null or empty"));
        } else if (!isString(email)) {
            errorList.add(new Error("email", "Email must be a string"));
        } else if (!isValidEmail(email)) {
            errorList.add(new Error("email", "Email is in an incorrect format"));
        }


        String password = loginRequest.getPassword();
        if (password == null || password.isEmpty()) {
            errorList.add(new Error("password", "Password must not be null or empty"));
        } else if (!isString(password)) {
            errorList.add(new Error("password", "Password must be a string"));
        }

        if (!errorList.isEmpty()) {
            errors.setErrors(errorList);
            return errors;
        }


        return null;
    }

    private boolean isString(Object obj) {
        return obj instanceof String;
    }

    public static boolean isValidEmail(String email) {
        String regex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
