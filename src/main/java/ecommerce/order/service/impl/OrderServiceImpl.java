package ecommerce.order.service.impl;

import ecommerce.cart.model.Cart;
import ecommerce.cart.model.CartItem;
import ecommerce.cart.repository.CartItemRepository;
import ecommerce.cart.repository.CartRepository;
import ecommerce.core.exception.APIException;
import ecommerce.core.exception.ResourceNotFoundException;
import ecommerce.inventory.service.InventoryService;
import ecommerce.order.dto.request.OrderRequest;
import ecommerce.order.dto.response.OrderItemResponse;
import ecommerce.order.dto.response.OrderResponse;
import ecommerce.order.model.ENUM.OrderStatus;
import ecommerce.order.model.Order;
import ecommerce.order.model.OrderItem;
import ecommerce.order.repository.OrderItemRepository;
import ecommerce.order.repository.OrderRepository;
import ecommerce.order.service.OrderService;
import ecommerce.product.model.Product;
import ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        log.debug("Placing order for userId={}", userId);
        if (userId == null) {
            throw new APIException("UserId cannot be null");
        }

        // ===================== 1. GET CART =====================
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart", "userId", userId
                ));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getCartId());

        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        // ===================== 2. CREATE ORDER (PENDING STATE) =====================
        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(request.getAddressId());
        order.setTotalAmount(cart.getTotalPrice());
        order.setStatus(OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);

        // IMPORTANT: initialize state
        savedOrder.initState();

        // ===================== 3. CREATE ORDER ITEMS (SNAPSHOT) =====================
        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> {

                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Product", "productId", item.getProductId()
                            ));

                    BigDecimal totalPrice = item.getPrice();

                    return new OrderItem(
                            null,
                            savedOrder.getOrderId(),
                            item.getProductId(),
                            product.getProductName(),
                            item.getQuantity(),
                            product.getPrice(),
                            product.getDiscount(),
                            totalPrice
                    );
                })
                .toList();

        orderItemRepository.saveAll(orderItems);

        // ===================== 4. MOVE STATE (PENDING → CONFIRMED) =====================
        savedOrder.next(); // State pattern call
        orderRepository.save(savedOrder);

        // ===================== 5. CLEAR CART =====================
        cartItemRepository.deleteAll(cartItems);

        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        // ===================== 6. RESPONSE BUILD =====================
        return OrderResponse.builder()
                .orderId(savedOrder.getOrderId())
                .userId(savedOrder.getUserId())
                .addressId(savedOrder.getAddressId())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus().name())
                .orderDate(savedOrder.getOrderDate())
                .items(
                        orderItems.stream()
                                .map(i -> OrderItemResponse.builder()
                                        .orderItemId(i.getOrderItemId())
                                        .productId(i.getProductId())
                                        .quantity(i.getQuantity())
                                        .price(i.getPrice())
                                        .discount(i.getDiscount())
                                        .totalPrice(i.getTotalPrice())
                                        .build()
                                ).toList()
                )
                .build();
    }

    @Override
    @Transactional
    public OrderResponse moveToNextState(Long orderId) {

        log.debug("Moving order to next state orderId={}", orderId);

        if (orderId == null) {
            throw new APIException("OrderId cannot be null");
        }

        // ===================== 1. FETCH ORDER =====================
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "orderId", orderId
                ));

        // ===================== 2. INIT STATE FROM DB STATUS =====================
        order.initState();

        OrderStatus previousStatus = order.getStatus();

        // ===================== 3. MOVE TO NEXT STATE =====================
        order.next();   // State pattern call

        // ===================== 4. SAVE UPDATED STATUS =====================
        Order updatedOrder = orderRepository.save(order);

        log.info("Order state changed orderId={} from {} to {}",
                orderId, previousStatus, updatedOrder.getStatus());

        // ===================== 5. FETCH ORDER ITEMS =====================
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // ===================== 6. RESPONSE BUILD =====================
        return OrderResponse.builder()
                .orderId(updatedOrder.getOrderId())
                .userId(updatedOrder.getUserId())
                .addressId(updatedOrder.getAddressId())
                .totalAmount(updatedOrder.getTotalAmount())
                .status(updatedOrder.getStatus().name())
                .orderDate(updatedOrder.getOrderDate())
                .items(
                        items.stream()
                                .map(i -> OrderItemResponse.builder()
                                        .orderItemId(i.getOrderItemId())
                                        .productId(i.getProductId())
                                        .quantity(i.getQuantity())
                                        .price(i.getPrice())
                                        .discount(i.getDiscount())
                                        .totalPrice(i.getTotalPrice())
                                        .build()
                                ).toList()
                )
                .build();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {

        log.debug("Cancelling order orderId={}, userId={}", orderId, userId);

        if (orderId == null || userId == null) {
            throw new APIException("OrderId and UserId cannot be null");
        }

        // ===================== 1. FETCH ORDER =====================
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "orderId", orderId
                ));

        // ===================== 2. OWNERSHIP CHECK =====================
        if (!order.getUserId().equals(userId)) {
            throw new APIException("You are not allowed to cancel this order");
        }

        // ===================== 3. INIT STATE =====================
        order.initState();

        OrderStatus previousStatus = order.getStatus();

        // ===================== 4. CANCEL VIA STATE PATTERN =====================
        try {
            order.cancel();   // State pattern call
        } catch (Exception e) {
            throw new APIException(e.getMessage());
        }

        Order updatedOrder = orderRepository.save(order);

        // ===================== 5. RELEASE INVENTORY (IMPORTANT) =====================
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : items) {
            inventoryService.releaseReservedStock(
                    item.getProductId(),
                    item.getQuantity()
            );
        }

        log.info("Order cancelled orderId={} from {} to {}",
                orderId, previousStatus, updatedOrder.getStatus());

        // ===================== 6. RESPONSE =====================
        return OrderResponse.builder()
                .orderId(updatedOrder.getOrderId())
                .userId(updatedOrder.getUserId())
                .addressId(updatedOrder.getAddressId())
                .totalAmount(updatedOrder.getTotalAmount())
                .status(updatedOrder.getStatus().name())
                .orderDate(updatedOrder.getOrderDate())
                .items(
                        items.stream()
                                .map(i -> OrderItemResponse.builder()
                                        .orderItemId(i.getOrderItemId())
                                        .productId(i.getProductId())
                                        .quantity(i.getQuantity())
                                        .price(i.getPrice())
                                        .discount(i.getDiscount())
                                        .totalPrice(i.getTotalPrice())
                                        .build()
                                ).toList()
                )
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(Long userId) {

        log.debug("Fetching all orders for userId={}", userId);

        if (userId == null) {
            throw new APIException("UserId cannot be null");
        }

        // ===================== 1. FETCH ORDERS =====================
        List<Order> orders = orderRepository.findByUserId(userId);

        if (orders.isEmpty()) {
            return List.of();
        }

        // ===================== 2. MAP EACH ORDER =====================
        return orders.stream().map(order -> {

            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());

            return OrderResponse.builder()
                    .orderId(order.getOrderId())
                    .userId(order.getUserId())
                    .addressId(order.getAddressId())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus().name())
                    .orderDate(order.getOrderDate())
                    .items(
                            items.stream()
                                    .map(i -> OrderItemResponse.builder()
                                            .orderItemId(i.getOrderItemId())
                                            .productId(i.getProductId())
                                            .quantity(i.getQuantity())
                                            .price(i.getPrice())
                                            .discount(i.getDiscount())
                                            .totalPrice(i.getTotalPrice())
                                            .build()
                                    ).toList()
                    )
                    .build();

        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {

        log.debug("Fetching orderId={} for userId={}", orderId, userId);

        if (userId == null || orderId == null) {
            throw new APIException("UserId and OrderId cannot be null");
        }

        // ===================== 1. FETCH ORDER =====================
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order", "orderId", orderId
                ));

        // ===================== 2. OWNERSHIP CHECK (SECURITY) =====================
        if (!order.getUserId().equals(userId)) {
            throw new APIException("You are not allowed to access this order");
        }

        // ===================== 3. FETCH ORDER ITEMS =====================
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // ===================== 4. BUILD RESPONSE =====================
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .addressId(order.getAddressId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .orderDate(order.getOrderDate())
                .items(
                        items.stream()
                                .map(i -> OrderItemResponse.builder()
                                        .orderItemId(i.getOrderItemId())
                                        .productId(i.getProductId())
                                        .quantity(i.getQuantity())
                                        .price(i.getPrice())
                                        .discount(i.getDiscount())
                                        .totalPrice(i.getTotalPrice())
                                        .build()
                                ).toList()
                )
                .build();
    }
}
