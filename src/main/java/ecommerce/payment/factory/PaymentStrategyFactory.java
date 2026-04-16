package ecommerce.payment.factory;

import ecommerce.payment.model.ENUM.PaymentMethod;
import ecommerce.payment.strategy.CardPaymentStrategy;
import ecommerce.payment.strategy.CodPaymentStrategy;
import ecommerce.payment.strategy.PaymentStrategy;
import ecommerce.payment.strategy.UpiPaymentStrategy;
import org.springframework.stereotype.Component;

@Component
public class PaymentStrategyFactory {

    private final UpiPaymentStrategy upiStrategy;
    private final CardPaymentStrategy cardStrategy;
    private final CodPaymentStrategy codStrategy;

    public PaymentStrategyFactory(UpiPaymentStrategy upiStrategy, CardPaymentStrategy cardStrategy, CodPaymentStrategy codStrategy) {
        this.upiStrategy = upiStrategy;
        this.cardStrategy = cardStrategy;
        this.codStrategy = codStrategy;
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {

        return switch (method) {
            case UPI -> upiStrategy;
            case CARD -> cardStrategy;
            case COD -> codStrategy;
        };
    }
}