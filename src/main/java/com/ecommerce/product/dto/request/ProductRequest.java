package com.ecommerce.product.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String productName;
    private String image;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal specialPrice;
}
