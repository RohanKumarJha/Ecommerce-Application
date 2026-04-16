package ecommerce.order.state;

import ecommerce.order.model.Order;
import ecommerce.order.model.ENUM.OrderStatus;

public class CancelledState implements OrderState {

    @Override
    public void next(Order order) {
        throw new RuntimeException("Cancelled order cannot proceed");
    }

    @Override
    public void cancel(Order order) {
        throw new RuntimeException("Order already cancelled");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}