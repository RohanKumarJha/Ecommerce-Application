package ecommerce.cart.service.impl;

import ecommerce.cart.dto.response.CartItemResponse;
import ecommerce.cart.dto.response.CartResponse;
import ecommerce.cart.model.Cart;
import ecommerce.cart.model.CartItem;
import ecommerce.cart.model.ENUM.CartOperation;
import ecommerce.cart.repository.CartItemRepository;
import ecommerce.cart.repository.CartRepository;
import ecommerce.cart.service.CartService;
import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.exception.APIException;
import ecommerce.core.exception.ResourceNotFoundException;
import ecommerce.inventory.service.InventoryService;
import ecommerce.product.model.Product;
import ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public CartResponse addProductToCart(Long userId, Long productId, Integer quantity) {
        log.debug("Add to cart userId={}, productId={}, quantity={}",
                userId, productId, quantity);
        if (userId == null || productId == null || quantity == null || quantity <= 0) {
            throw new APIException("Invalid request data");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", productId
                ));
        inventoryService.reserveStock(productId, quantity);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .userId(userId)
                                .totalPrice(BigDecimal.ZERO)
                                .build()
                ));
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getCartId(), productId)
                .orElse(null);
        if (cartItem == null) {
            cartItem = CartItem.builder()
                    .cartId(cart.getCartId())
                    .productId(productId)
                    .quantity(quantity)
                    .price(product.getSpecialPrice())
                    .build();
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        }
        BigDecimal itemPrice = product.getSpecialPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        cartItem.setPrice(itemPrice);
        cartItemRepository.save(cartItem);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());
        BigDecimal total = items.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalPrice(total);
        cartRepository.save(cart);
        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();
        log.info("Product added to cart with reservation userId={}", userId);
        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .totalPrice(cart.getTotalPrice())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartByUser(Long userId) {
        log.debug("Fetching cart for userId={}", userId);
        if (userId == null) {
            throw new APIException("UserId cannot be null");
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart", "userId", userId
                ));
        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());
        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()
                )
                .toList();
        log.info("Cart fetched successfully for userId={} with {} items",
                userId, items.size());
        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .totalPrice(cart.getTotalPrice())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional
    public CartResponse updateProductQuantityInCart(Long userId, Long productId, CartOperation operation) {
        log.debug("Update cart quantity userId={}, productId={}, operation={}",
                userId, productId, operation);
        if (userId == null || productId == null || operation == null) {
            throw new APIException("Invalid request data");
        }
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart", "userId", userId
                ));
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CartItem", "productId", productId
                ));
        int quantity = cartItem.getQuantity();

        // ================= INCREASE =================
        if (operation == CartOperation.INCREASE) {
            inventoryService.reserveStock(productId, 1);
            cartItem.setQuantity(quantity + 1);
        } else if (operation == CartOperation.DECREASE) {
            inventoryService.releaseReservedStock(productId, 1);
            cartItem.setQuantity(quantity - 1);
        } else {
            throw new APIException("Invalid operation");
        }

        // update item price
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "productId", productId
                ));

        cartItem.setPrice(
                product.getSpecialPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );

        cartItemRepository.save(cartItem);

        // recalc cart total
        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());

        BigDecimal total = items.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(total);

        cartRepository.save(cart);

        // response build
        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getCartItemId())
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .toList();

        log.info("Cart updated successfully userId={}", userId);

        return CartResponse.builder()
                .cartId(cart.getCartId())
                .userId(cart.getUserId())
                .totalPrice(cart.getTotalPrice())
                .items(itemResponses)
                .build();
    }

    @Override
    @Transactional
    public MessageResponse deleteProductFromCart(Long userId, Long productId) {

        log.debug("Removing product from cart userId={}, productId={}", userId, productId);

        if (userId == null || productId == null) {
            throw new APIException("Invalid request data");
        }

        // 1. Fetch cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart", "userId", userId
                ));

        // 2. Fetch cart item
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CartItem", "productId", productId
                ));

        int quantity = cartItem.getQuantity();

        // 3. Release reserved stock back to inventory
        inventoryService.releaseReservedStock(productId, quantity);

        // 4. Delete cart item
        cartItemRepository.delete(cartItem);

        // 5. Recalculate cart total
        List<CartItem> items = cartItemRepository.findByCartId(cart.getCartId());

        BigDecimal total = items.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(total);

        cartRepository.save(cart);

        log.info("Product removed from cart userId={}, productId={}", userId, productId);

        return new MessageResponse("Product removed from cart successfully");
    }
}
