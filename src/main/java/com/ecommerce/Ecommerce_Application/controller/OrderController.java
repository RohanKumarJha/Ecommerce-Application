package com.ecommerce.Ecommerce_Application.controller;

import com.ecommerce.Ecommerce_Application.payload.request.OrderRequest;
import com.ecommerce.Ecommerce_Application.payload.response.OrderResponse;
import com.ecommerce.Ecommerce_Application.service.OrderService;
import com.ecommerce.Ecommerce_Application.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AuthUtil authUtil;

    @Transactional
    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderResponse> orderProducts(@PathVariable String paymentMethod, @RequestBody OrderRequest orderRequestDTO) {
        String emailId = authUtil.loggedInEmail();
        OrderResponse order = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
