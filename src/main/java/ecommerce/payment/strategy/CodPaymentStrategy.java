package ecommerce.payment.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CodPaymentStrategy implements PaymentStrategy {

    @Override
    public String pay(Long orderId, BigDecimal amount) {
        return "COD_ORDER_PLACED_" + orderId;
    }
}