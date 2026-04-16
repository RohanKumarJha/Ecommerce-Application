package ecommerce.cart.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private Integer quantity;
    private BigDecimal price;
}