package ecommerce.order.state;

import ecommerce.order.model.ENUM.OrderStatus;
import ecommerce.order.model.Order;

public interface OrderState {

    void next(Order order);

    void cancel(Order order);

    OrderStatus getStatus();
}