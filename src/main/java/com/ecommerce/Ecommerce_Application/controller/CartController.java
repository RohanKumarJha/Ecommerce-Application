package com.ecommerce.Ecommerce_Application.controller;

import com.ecommerce.Ecommerce_Application.model.Cart;
import com.ecommerce.Ecommerce_Application.payload.response.CartResponse;
import com.ecommerce.Ecommerce_Application.repository.CartRepository;
import com.ecommerce.Ecommerce_Application.service.CartService;
import com.ecommerce.Ecommerce_Application.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CartRepository cartRepository;

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartResponse> addProductToCart(@PathVariable Long productId,
                                                         @PathVariable Integer quantity) {
        CartResponse cartResponse = cartService.addProductToCart(productId,quantity);
        return new ResponseEntity<>(cartResponse, HttpStatus.CREATED);
    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartResponse>> getCarts() {
        List<CartResponse> categoryResponses = cartService.getAllCarts();
        return new ResponseEntity<>(categoryResponses, HttpStatus.FOUND);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartResponse> getCartById(){
        String emailId = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);
        Long cartId = cart.getCartId();
        CartResponse cartDTO = cartService.getCart(emailId, cartId);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartResponse> updateCartProduct(@PathVariable Long productId,
                                                     @PathVariable String operation) {
        CartResponse cartDTO = cartService.updateProductQuantityInCart(productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId,
                                                        @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
