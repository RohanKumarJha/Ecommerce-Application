package com.ecommerce.Ecommerce_Application.repository;


import com.ecommerce.Ecommerce_Application.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>{

}