package ecommerce.order.state;

import ecommerce.order.model.Order;
import ecommerce.order.model.ENUM.OrderStatus;

public class DeliveredState implements OrderState {

    @Override
    public void next(Order order) {
        throw new RuntimeException("Order already delivered");
    }

    @Override
    public void cancel(Order order) {
        throw new RuntimeException("Cannot cancel delivered order");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.DELIVERED;
    }
}