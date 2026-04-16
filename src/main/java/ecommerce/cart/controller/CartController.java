package ecommerce.cart.controller;

import ecommerce.cart.dto.request.CartItemRequest;
import ecommerce.cart.dto.response.CartResponse;
import ecommerce.cart.model.ENUM.CartOperation;
import ecommerce.cart.service.CartService;
import ecommerce.core.dto.response.MessageResponse;
import ecommerce.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService service;

    // ===================== ADD PRODUCT TO CART =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/products")
    public ResponseEntity<CartResponse> addProduct(
            @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.addProductToCart(
                        user.getUserId(),
                        request.getProductId(),
                        request.getQuantity()
                ));
    }

    // ===================== GET CART BY USER =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetailsImpl user) {

        return ResponseEntity.ok(
                service.getCartByUser(user.getUserId())
        );
    }

    // ===================== UPDATE PRODUCT QUANTITY =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/products/{productId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable Long productId,
            @RequestParam CartOperation operation,
            @AuthenticationPrincipal UserDetailsImpl user) {

        return ResponseEntity.ok(
                service.updateProductQuantityInCart(
                        user.getUserId(),
                        productId,
                        operation
                )
        );
    }

    // ===================== REMOVE PRODUCT FROM CART =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<MessageResponse> removeProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetailsImpl user) {

        return ResponseEntity.ok(
                service.deleteProductFromCart(
                        user.getUserId(),
                        productId
                )
        );
    }

}