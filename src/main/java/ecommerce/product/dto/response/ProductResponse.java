package ecommerce.product.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long productId;
    private String productName;
    private String description;
    private String image;

    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal specialPrice;

    private Long sellerId;
    private Long categoryId;
}