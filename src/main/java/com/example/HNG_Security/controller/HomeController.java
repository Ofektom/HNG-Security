package com.example.HNG_Security.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<String> homePage(){
        String welcome = "Welcome to HNG stage 2 security application by Ofofonono Okpoho";
        return new ResponseEntity<>(welcome, HttpStatus.OK);
    }
}
