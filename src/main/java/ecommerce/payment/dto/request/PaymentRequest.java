package ecommerce.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment method is required")
    @Pattern(
            regexp = "COD|UPI|CARD",
            message = "Payment method must be COD, UPI, or CARD"
    )
    private String paymentMethod; // COD / UPI / CARD

    // ❗ Set internally from JWT, NOT from frontend
    private Long userId;
}