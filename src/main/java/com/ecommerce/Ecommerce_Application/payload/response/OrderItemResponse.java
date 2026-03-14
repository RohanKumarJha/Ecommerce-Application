package com.ecommerce.Ecommerce_Application.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

    private Long orderItemId;
    private ProductResponse product;
    private Integer quantity;
    private double discount;
    private double orderedProductPrice;

}