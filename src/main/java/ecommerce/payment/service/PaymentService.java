package ecommerce.payment.service;

import ecommerce.payment.dto.request.PaymentRequest;
import ecommerce.payment.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse processPayment(PaymentRequest request);

    PaymentResponse getPaymentById(Long paymentId);

    PaymentResponse getPaymentByOrderId(Long orderId);
}