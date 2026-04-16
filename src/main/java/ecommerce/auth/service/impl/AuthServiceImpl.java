package ecommerce.auth.service.impl;

import ecommerce.auth.dto.request.LoginRequest;
import ecommerce.auth.dto.request.SignupRequest;
import ecommerce.auth.dto.response.LoginResponse;
import ecommerce.auth.service.AuthService;
import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.exception.APIException;
import ecommerce.security.jwt.JwtUtils;
import ecommerce.security.services.UserDetailsImpl;
import ecommerce.user.model.ENUM.AppRole;
import ecommerce.user.model.Role;
import ecommerce.user.model.User;
import ecommerce.user.repository.RoleRepository;
import ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    // ---------------- REGISTER ----------------
    @Override
    public MessageResponse registerUser(SignupRequest request) {
        logger.info("Register request received for username: {}", request.getUserName());
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new APIException("Username is already in use");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new APIException("Email is already in use");
        }
        Set<Role> roles = mapRoles(request.getRole());
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRoles(roles);
        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUserName());
        return new MessageResponse("User registered successfully");
    }

    // ---------------- ROLE MAPPING ----------------
    private Set<Role> mapRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();
        if (strRoles == null || strRoles.isEmpty()) {
            roles.add(getRole(AppRole.ROLE_USER));
            return roles;
        }
        for (String roleStr : strRoles) {
            try {
                AppRole appRole = AppRole.valueOf(roleStr.toUpperCase());
                roles.add(getRole(appRole));
            } catch (IllegalArgumentException ex) {
                throw new APIException("Invalid role: " + roleStr);
            }
        }
        return roles;
    }

    private Role getRole(AppRole roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new APIException(roleName + " not found in DB"));
    }

    // ---------------- LOGIN ----------------
    @Override
    public LoginResponse authenticateUser(LoginRequest request) {
        logger.info("Login attempt for username: {}", request.getUserName());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUserName(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails);
            Set<String> roles = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            return new LoginResponse(
                    userDetails.getUserId(),
                    jwt,
                    userDetails.getUsername(),
                    roles
            );
        } catch (BadCredentialsException e) {
            logger.error("Invalid login credentials for: {}", request.getUserName());
            throw new APIException("Invalid username or password.");
        } catch (Exception e) {
            logger.error("Login error for username: {}", request.getUserName(), e);
            throw new APIException("Login failed. Please try again later.");
        }
    }
}