package com.ecommerce.user.repository;

import com.ecommerce.user.model.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Optional<User> findByUserName(String username);

    boolean existsByUserName(@NotBlank @Size(min = 3, max = 20) String userName);
}