package com.ecommerce.Ecommerce_Application.service;

import com.ecommerce.Ecommerce_Application.payload.response.CartResponse;

import java.util.List;

public interface CartService {
    CartResponse addProductToCart(Long productId, Integer quantity);
    List<CartResponse> getAllCarts();
    CartResponse getCart(String emailId, Long cartId);
    CartResponse updateProductQuantityInCart(Long productId, Integer quantity);
    String deleteProductFromCart(Long cartId, Long productId);
    void updateProductInCarts(Long cartId, Long productId);
}
