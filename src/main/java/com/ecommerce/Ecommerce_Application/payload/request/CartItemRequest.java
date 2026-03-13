package com.ecommerce.Ecommerce_Application.payload.request;

import com.ecommerce.Ecommerce_Application.payload.response.CartResponse;
import com.ecommerce.Ecommerce_Application.payload.response.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    private CartResponse cart;
    private ProductResponse product;
    private Integer quantity;
    private Double discount;
    private Double productPrice;
}