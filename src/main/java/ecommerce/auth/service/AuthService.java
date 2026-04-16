package ecommerce.auth.service;

import ecommerce.auth.dto.request.LoginRequest;
import ecommerce.auth.dto.request.SignupRequest;
import ecommerce.auth.dto.response.LoginResponse;
import ecommerce.core.dto.response.MessageResponse;

public interface AuthService {
    MessageResponse registerUser(SignupRequest signUpRequest);

    LoginResponse authenticateUser(LoginRequest loginRequest);
}