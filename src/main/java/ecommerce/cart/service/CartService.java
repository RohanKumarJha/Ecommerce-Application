package ecommerce.cart.service;

import ecommerce.cart.dto.response.CartResponse;
import ecommerce.cart.model.ENUM.CartOperation;
import ecommerce.core.dto.response.MessageResponse;

public interface CartService {
    CartResponse addProductToCart(Long userId, Long productId, Integer quantity);

    CartResponse getCartByUser(Long userId);

    CartResponse updateProductQuantityInCart(Long userId, Long productId, CartOperation operation);

    MessageResponse deleteProductFromCart(Long userId, Long productId);
}
