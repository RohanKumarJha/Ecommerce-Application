package ecommerce.payment.controller;

import ecommerce.payment.dto.request.PaymentRequest;
import ecommerce.payment.dto.response.PaymentResponse;
import ecommerce.payment.service.PaymentService;
import ecommerce.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    // ===================== PROCESS PAYMENT =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {

        request.setUserId(user.getUserId());

        return ResponseEntity.status(201)
                .body(service.processPayment(request));
    }

    // ===================== GET PAYMENT BY ID =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                service.getPaymentById(paymentId)
        );
    }

    // ===================== GET PAYMENT BY ORDER ID =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            @PathVariable Long orderId) {

        return ResponseEntity.ok(
                service.getPaymentByOrderId(orderId)
        );
    }
}