package ecommerce.order.controller;

import ecommerce.order.dto.request.OrderRequest;
import ecommerce.order.dto.response.OrderResponse;
import ecommerce.order.service.OrderService;
import ecommerce.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    // ===================== PLACE ORDER =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody OrderRequest request) {

        return ResponseEntity.status(201)
                .body(service.placeOrder(user.getUserId(), request));
    }

    // ===================== MOVE ORDER TO NEXT STATE =====================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/next")
    public ResponseEntity<OrderResponse> moveToNextState(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.moveToNextState(id)
        );
    }

    // ===================== CANCEL ORDER =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl user) {

        // optional: you can validate ownership in service using user.getUserId()
        return ResponseEntity.ok(
                service.cancelOrder(id, user.getUserId())
        );
    }

    // ===================== GET MY ORDERS =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetailsImpl user) {

        return ResponseEntity.ok(
                service.getAllOrders(user.getUserId())
        );
    }

    // ===================== GET ORDER BY ID (SECURED) =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetailsImpl user) {

        return ResponseEntity.ok(
                service.getOrderById(user.getUserId(), orderId)
        );
    }

}