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

    private final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

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
        log.info("Attempting to register user: {}", signUpRequest.getUserName());

        // ✅ Validation
        if (userRepository.existsByUserName(signUpRequest.getUserName())) {
            throw new APIException("Username is already in use");
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new APIException("Email is already in use");
        }

        // ✅ Role Mapping
        Set<Role> roles = mapRoles(signUpRequest.getRole());

        // ✅ Factory Usage 🔥
        User user = UserFactory.createUser(
                signUpRequest.getUserName(),
                signUpRequest.getEmail(),
                signUpRequest.getPassword(),
                roles,
                encoder
        );
        userRepository.save(user);
        log.info("User registered successfully: {}", signUpRequest.getUserName());
        return "User registered successfully";
    }

    // ---------------------- ROLE MAPPING (CLEAN SEPARATION) ----------------------
    private Set<Role> mapRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();
        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "ROLE_ADMIN" -> roles.add(roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role not found")));
                    case "ROLE_SELLER" -> roles.add(roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                            .orElseThrow(() -> new RuntimeException("Error: Role not found")));
                    default -> roles.add(roleRepository.findByRoleName(AppRole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role not found")));
                }
            });
        }
        return roles;
    }

    // ---------------------- LOGIN ----------------------
    @Override
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUserName());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserName(),
                            loginRequest.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            throw new APIException("Invalid username or password");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwtToken = jwtUtils.generateJwtTokenFromUserName(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
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
        if (authentication == null) {
            throw new APIException("Authentication object is null");
        }
        return new MessageResponse("UserName is " + authentication.getName());
    }

    // ---------------------- USER DETAILS ----------------------
    @Override
    public UserInfoResponse getUserDetails(Authentication authentication) {
        if (authentication == null) {
            throw new APIException("Authentication object is null");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return new UserInfoResponse(
                userDetails.getUserId(),
                userDetails.getUsername(),
                roles
        );
    }
}