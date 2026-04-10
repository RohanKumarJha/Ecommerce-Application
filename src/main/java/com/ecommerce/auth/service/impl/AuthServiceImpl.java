package com.ecommerce.auth.service.impl;

import com.ecommerce.auth.dto.request.LoginRequest;
import com.ecommerce.auth.dto.request.SignupRequest;
import com.ecommerce.auth.dto.response.LoginResponse;
import com.ecommerce.auth.dto.response.MessageResponse;
import com.ecommerce.auth.dto.response.UserInfoResponse;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.core.exception.APIException;
import com.ecommerce.core.security.jwt.JwtUtils;
import com.ecommerce.core.security.services.UserDetailsImpl;
import com.ecommerce.user.factory.UserFactory;
import com.ecommerce.user.model.ENUM.AppRole;
import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.User;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    // ---------------------- REGISTER USER ----------------------
    @Override
    public String registerUser(SignupRequest signUpRequest) {
        log.info("Register request received for username: {}", signUpRequest.getUserName());
        if (userRepository.existsByUserName(signUpRequest.getUserName())) {
            log.warn("Registration failed: Username already exists: {}", signUpRequest.getUserName());
            throw new APIException("Username is already in use");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.warn("Registration failed: Email already exists: {}", signUpRequest.getEmail());
            throw new APIException("Email is already in use");
        }
        Set<Role> roles = mapRoles(signUpRequest.getRole());
        log.debug("Roles mapped for user {}: {}", signUpRequest.getUserName(), roles);
        User user = UserFactory.createUser(
                signUpRequest.getUserName(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword(),
                roles,
                encoder
        );
        userRepository.save(user);
        log.info("User successfully registered with username: {}", signUpRequest.getUserName());
        return "User registered successfully";
    }

    // ---------------------- ROLE MAPPING ----------------------
    private Set<Role> mapRoles(Set<String> strRoles) {
        log.debug("Mapping roles: {}", strRoles);
        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty()) {
            log.info("No roles provided, assigning default ROLE_USER");
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> {
                        log.error("ROLE_USER not found in database");
                        return new RuntimeException("Error: Role is not found.");
                    });
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN" -> {
                        log.debug("Assigning ROLE_ADMIN");
                        roles.add(roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> {
                                    log.error("ROLE_ADMIN not found in database");
                                    return new RuntimeException("Error: Role not found");
                                }));
                    }
                    case "ROLE_SELLER" -> {
                        log.debug("Assigning ROLE_SELLER");
                        roles.add(roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> {
                                    log.error("ROLE_SELLER not found in database");
                                    return new RuntimeException("Error: Role not found");
                                }));
                    }
                    default -> {
                        log.debug("Assigning default ROLE_USER");
                        roles.add(roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> {
                                    log.error("ROLE_USER not found in database");
                                    return new RuntimeException("Error: Role not found");
                                }));
                    }
                }
            });
        }
        return roles;
    }

    // ---------------------- LOGIN ----------------------
    @Override
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUserName());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserName(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            log.error("Authentication failed for username: {}", loginRequest.getUserName());
            throw new APIException("Invalid username or password");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateJwtTokenFromUserName(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        log.info("User logged in successfully: {}", userDetails.getUsername());
        return new LoginResponse(
                userDetails.getUserId(),
                jwtToken,
                userDetails.getUsername(),
                roles
        );
    }

    // ---------------------- CURRENT USERNAME ----------------------
    @Override
    public MessageResponse currentUserName(Authentication authentication) {
        log.debug("Fetching current username");
        if (authentication == null) {
            log.error("Authentication object is null while fetching username");
            throw new APIException("Authentication object is null");
        }
        return new MessageResponse("UserName is " + authentication.getName());
    }

    // ---------------------- USER DETAILS ----------------------
    @Override
    public UserInfoResponse getUserDetails(Authentication authentication) {
        log.debug("Fetching user details");
        if (authentication == null) {
            log.error("Authentication object is null while fetching user details");
            throw new APIException("Authentication object is null");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        log.info("User details fetched for username: {}", userDetails.getUsername());
        return new UserInfoResponse(
                userDetails.getUserId(),
                userDetails.getUsername(),
                roles
        );
    }
}