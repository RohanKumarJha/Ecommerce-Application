package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.request.LoginRequest;
import com.ecommerce.auth.dto.request.SignupRequest;
import com.ecommerce.auth.dto.response.LoginResponse;
import com.ecommerce.auth.dto.response.MessageResponse;
import com.ecommerce.auth.dto.response.UserInfoResponse;
import com.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Base URL for auth APIs
public class AuthController {

    private final Logger log = LoggerFactory.getLogger(AuthController.class); // Logger

    @Autowired
    private AuthService authService; // Auth service

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(
            @Valid @RequestBody SignupRequest signUpRequest) {
        log.info("Register request received for username: {}", signUpRequest.getUserName()); // Log signup
        String response = authService.registerUser(signUpRequest);
        log.info("User registered successfully: {}", signUpRequest.getUserName());
        return new ResponseEntity<>(new MessageResponse(response), HttpStatus.CREATED); // Return 201
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for username: {}", loginRequest.getUserName()); // Log login
        LoginResponse response = authService.authenticateUser(loginRequest);
        log.info("User authenticated successfully: {}", loginRequest.getUserName());
        return new ResponseEntity<>(response, HttpStatus.OK); // Return 200
    }

    @GetMapping("/userName")
    public ResponseEntity<MessageResponse> currentUserName(Authentication authentication) {
        log.info("Fetching current username"); // Get logged-in username
        MessageResponse response = authService.currentUserName(authentication);
        log.info("Username fetched successfully");
        return new ResponseEntity<>(response, HttpStatus.OK); // Return 200
    }

    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> getUserDetails(Authentication authentication) {
        log.info("Fetching user details"); // Get user details
        UserInfoResponse response = authService.getUserDetails(authentication);
        log.info("User details fetched for user: {}", authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.OK); // Return 200
    }
}