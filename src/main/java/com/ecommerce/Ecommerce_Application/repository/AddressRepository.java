package com.ecommerce.Ecommerce_Application.repository;

import com.ecommerce.Ecommerce_Application.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
