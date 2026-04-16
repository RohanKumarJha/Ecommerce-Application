package ecommerce.order.state;

import ecommerce.order.model.Order;
import ecommerce.order.model.ENUM.OrderStatus;

public class ConfirmedState implements OrderState {

    @Override
    public void next(Order order) {
        order.setState(new ShippedState());
    }

    @Override
    public void cancel(Order order) {
        order.setState(new CancelledState());
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CONFIRMED;
    }
}