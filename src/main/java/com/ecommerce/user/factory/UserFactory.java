package com.ecommerce.user.factory;

import com.ecommerce.user.model.Role;
import com.ecommerce.user.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

public class UserFactory {
    public static User createUser(String name, String email, String password, Set<Role> roles, PasswordEncoder encoder) {
        User user = new User();
        user.setUserName(name);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setRoles(roles);
        return user;
    }
}