package ecommerce.user.service;

import ecommerce.core.dto.response.MessageResponse;
import ecommerce.user.dto.request.UserRequest;
import ecommerce.user.dto.response.UserResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {

    UserResponse getCurrentUser(Authentication authentication);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Authentication authentication, UserRequest request);

    MessageResponse deleteUser(Long id);
}