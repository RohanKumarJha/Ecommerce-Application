package ecommerce.payment.strategy;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CardPaymentStrategy implements PaymentStrategy {

    @Override
    public String pay(Long orderId, BigDecimal amount) {
        return "CARD_PAYMENT_SUCCESS_" + orderId;
    }
}