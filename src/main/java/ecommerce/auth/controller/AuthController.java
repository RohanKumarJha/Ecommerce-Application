package ecommerce.auth.controller;

import ecommerce.auth.dto.request.LoginRequest;
import ecommerce.auth.dto.request.SignupRequest;
import ecommerce.auth.dto.response.LoginResponse;
import ecommerce.auth.service.AuthService;
import ecommerce.core.dto.response.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> registerUser(
            @Valid @RequestBody SignupRequest request) {
        log.info("Register request for username: {}", request.getUserName());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registerUser(request));
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> authenticateUser(
            @Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUserName());
        LoginResponse response = authService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }
}