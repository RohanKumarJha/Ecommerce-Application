package com.ecommerce.Ecommerce_Application.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Long cartId;
    private Double totalPrice = 0.0;
    private List<ProductResponse> products = new ArrayList<>();
}