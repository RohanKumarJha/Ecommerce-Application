package ecommerce.user.controller;

import ecommerce.core.dto.response.MessageResponse;
import ecommerce.user.dto.request.UserRequest;
import ecommerce.user.dto.response.UserResponse;
import ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        log.info("Fetching current user");
        return ResponseEntity.ok(userService.getCurrentUser(authentication));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateUser(
            Authentication authentication,
            @Valid @RequestBody UserRequest request) {
        log.info("Updating current user");
        return ResponseEntity.ok(userService.updateUser(authentication, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Admin fetching user {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin fetching all users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long id) {
        log.info("Admin deleting user {}", id);
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}