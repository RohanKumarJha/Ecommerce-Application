package com.ecommerce.Ecommerce_Application.service;

import com.ecommerce.Ecommerce_Application.payload.response.OrderResponse;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface OrderService {
    OrderResponse placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage);
}
