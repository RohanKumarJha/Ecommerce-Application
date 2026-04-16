package ecommerce.order.state;

import ecommerce.order.model.Order;
import ecommerce.order.model.ENUM.OrderStatus;

public class ShippedState implements OrderState {

    @Override
    public void next(Order order) {
        order.setState(new DeliveredState());
    }

    @Override
    public void cancel(Order order) {
        throw new RuntimeException("Cannot cancel after shipping");
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.SHIPPED;
    }
}