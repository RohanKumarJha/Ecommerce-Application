package ecommerce.payment.strategy;

import java.math.BigDecimal;

public interface PaymentStrategy {

    String pay(Long orderId, BigDecimal amount);

}