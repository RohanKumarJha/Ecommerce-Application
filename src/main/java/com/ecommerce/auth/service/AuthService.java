package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.request.LoginRequest;
import com.ecommerce.auth.dto.request.SignupRequest;
import com.ecommerce.auth.dto.response.LoginResponse;
import com.ecommerce.auth.dto.response.MessageResponse;
import com.ecommerce.auth.dto.response.UserInfoResponse;
import org.springframework.security.core.Authentication;

public interface AuthService {
    String registerUser(SignupRequest signUpRequest);

    LoginResponse authenticateUser(LoginRequest loginRequest);

    MessageResponse currentUserName(Authentication authentication);

    UserInfoResponse getUserDetails(Authentication authentication);
}