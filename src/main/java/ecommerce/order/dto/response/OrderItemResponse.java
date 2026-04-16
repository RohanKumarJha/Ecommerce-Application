package ecommerce.order.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long orderItemId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal totalPrice;
}