package ecommerce.security.services;

import ecommerce.user.model.User;
import ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        logger.info("Loading user by username: {}", username);
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> {
                    logger.warn("Username {} not found", username);
                    return new UsernameNotFoundException("User Not Found with username: " + username);
                });
        return UserDetailsImpl.build(user);
    }
}