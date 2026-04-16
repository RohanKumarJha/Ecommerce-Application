package ecommerce.order.service;

import ecommerce.order.dto.request.OrderRequest;
import ecommerce.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(Long userId, OrderRequest request);

    OrderResponse moveToNextState(Long orderId);

    OrderResponse cancelOrder(Long orderId, Long userId);

    List<OrderResponse> getAllOrders(Long userId);

    OrderResponse getOrderById(Long userId, Long orderId);
}