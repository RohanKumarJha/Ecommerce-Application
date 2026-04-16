package ecommerce.user.service.impl;


import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.exception.APIException;
import ecommerce.core.exception.ResourceNotFoundException;
import ecommerce.security.services.UserDetailsImpl;
import ecommerce.user.dto.request.UserRequest;
import ecommerce.user.dto.response.UserResponse;
import ecommerce.user.model.User;
import ecommerce.user.repository.UserRepository;
import ecommerce.user.service.UserService;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository,
                           ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;

        // 🔥 Production-safe config
        this.modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
    }

    // ✅ Get current user
    @Override
    public UserResponse getCurrentUser(Authentication authentication) {

        logger.info("Fetching current authenticated user");

        if (authentication == null) {
            logger.error("Authentication object is null");
            throw new APIException("Authentication object is null");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Set<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        UserResponse response = modelMapper.map(userDetails, UserResponse.class);
        response.setUserName(userDetails.getUsername());
        response.setRoles(roles);

        return response;
    }

    // ✅ Get user by ID
    @Override
    public UserResponse getUserById(Long id) {

        logger.info("Fetching user by id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", id));

        return convertToResponse(user);
    }

    // ✅ Get all users
    @Override
    public List<UserResponse> getAllUsers() {

        logger.info("Fetching all users");

        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();

        logger.info("Total users fetched: {}", users.size());

        return users;
    }

    // ✅ Update current user
    @Override
    public UserResponse updateUser(Authentication authentication, UserRequest request) {

        logger.info("Updating user profile");

        String username = authentication.getName();

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userName", username));

        // 🔥 SAFE UPDATE (no null overwrite)
        modelMapper.map(request, user);

        User updatedUser = userRepository.save(user);

        logger.info("User updated successfully: userId={}", updatedUser.getUserId());

        return convertToResponse(updatedUser);
    }

    // ✅ Delete user
    @Override
    public MessageResponse deleteUser(Long id) {

        logger.info("Deleting userId={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", id));

        userRepository.delete(user);

        logger.info("User deleted successfully: userId={}", id);

        return new MessageResponse("User deleted successfully");
    }

    // ================= HELPER =================

    private UserResponse convertToResponse(User user) {

        if (user == null) {
            throw new APIException("User cannot be null");
        }

        UserResponse response = modelMapper.map(user, UserResponse.class);

        // 🔥 Manual mapping for roles (IMPORTANT)
        Set<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getRoleName().name())
                .collect(Collectors.toSet());

        response.setRoles(roles);

        return response;
    }
}