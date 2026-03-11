package com.ecommerce.Ecommerce_Application.repository;

import com.ecommerce.Ecommerce_Application.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
}
