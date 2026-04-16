package ecommerce.payment.service.impl;

import ecommerce.core.exception.APIException;
import ecommerce.core.exception.ResourceNotFoundException;
import ecommerce.inventory.service.InventoryService;
import ecommerce.order.model.Order;
import ecommerce.order.model.OrderItem;
import ecommerce.order.repository.OrderItemRepository;
import ecommerce.order.repository.OrderRepository;
import ecommerce.payment.dto.request.PaymentRequest;
import ecommerce.payment.dto.response.PaymentResponse;
import ecommerce.payment.factory.PaymentStrategyFactory;
import ecommerce.payment.model.ENUM.PaymentMethod;
import ecommerce.payment.model.ENUM.PaymentStatus;
import ecommerce.payment.model.Payment;
import ecommerce.payment.repository.PaymentRepository;
import ecommerce.payment.service.PaymentService;
import ecommerce.payment.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final OrderRepository orderRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.debug("Processing payment for orderId={}", request.getOrderId());
        if (request.getOrderId() == null || request.getPaymentMethod() == null) {
            throw new APIException("OrderId and Payment Method are required");
        }

        // ===================== 1. FETCH ORDER =====================
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "orderId", request.getOrderId()
                ));

        // ===================== 2. GET PAYMENT STRATEGY =====================
        PaymentMethod method = PaymentMethod.valueOf(request.getPaymentMethod());

        PaymentStrategy strategy = paymentStrategyFactory.getStrategy(method);

        // ===================== 3. EXECUTE PAYMENT =====================
        String transactionId = strategy.pay(order.getOrderId(), order.getTotalAmount());

        // (For now mock success/failure logic)
        boolean isSuccess = transactionId != null && !transactionId.contains("FAILED");

        PaymentStatus paymentStatus = isSuccess
                ? PaymentStatus.SUCCESS
                : PaymentStatus.FAILED;

        // ===================== 4. SAVE PAYMENT =====================
        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(paymentStatus);
        payment.setTransactionId(transactionId);

        Payment savedPayment = paymentRepository.save(payment);

        // ===================== 5. ORDER + INVENTORY HANDLING =====================
        order.initState();

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());

        if (paymentStatus == PaymentStatus.SUCCESS) {

            // ORDER CONFIRM
            order.next(); // PENDING → CONFIRMED

            // FINAL INVENTORY DEDUCTION (reserved → sold)
            for (OrderItem item : items) {
                inventoryService.decreaseStock(item.getProductId(), item.getQuantity());
            }

        } else {

            // ORDER CANCEL
            order.cancel();

            // RELEASE RESERVED STOCK (reserved → available)
            for (OrderItem item : items) {
                inventoryService.releaseReservedStock(item.getProductId(), item.getQuantity());
            }
        }

        orderRepository.save(order);

        log.info("Payment processed paymentId={}, orderId={}, status={}",
                savedPayment.getPaymentId(),
                order.getOrderId(),
                paymentStatus);

        // ===================== 6. RESPONSE =====================
        return PaymentResponse.builder()
                .paymentId(savedPayment.getPaymentId())
                .orderId(savedPayment.getOrderId())
                .amount(savedPayment.getAmount())
                .paymentMethod(savedPayment.getPaymentMethod().name())
                .paymentStatus(savedPayment.getPaymentStatus().name())
                .transactionId(savedPayment.getTransactionId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {

        log.debug("Fetching payment by paymentId={}", paymentId);

        if (paymentId == null) {
            throw new APIException("PaymentId cannot be null");
        }

        // ===================== 1. FETCH PAYMENT =====================
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "paymentId", paymentId
                ));

        // ===================== 2. BUILD RESPONSE =====================
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .paymentStatus(payment.getPaymentStatus().name())
                .transactionId(payment.getTransactionId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {

        log.debug("Fetching payment by orderId={}", orderId);

        if (orderId == null) {
            throw new APIException("OrderId cannot be null");
        }

        // ===================== 1. FETCH PAYMENT =====================
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "orderId", orderId
                ));

        // ===================== 2. BUILD RESPONSE =====================
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod().name())
                .paymentStatus(payment.getPaymentStatus().name())
                .transactionId(payment.getTransactionId())
                .build();
    }
}
