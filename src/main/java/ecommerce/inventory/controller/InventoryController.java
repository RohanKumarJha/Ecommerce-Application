package ecommerce.inventory.controller;

import ecommerce.core.dto.response.MessageResponse;
import ecommerce.inventory.dto.request.InventoryRequest;
import ecommerce.inventory.dto.response.InventoryResponse;
import ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    // ===================== CREATE / UPDATE INVENTORY =====================
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @PostMapping
    public ResponseEntity<InventoryResponse> createOrUpdate(
            @RequestBody InventoryRequest request) {

        return ResponseEntity.status(201)
                .body(service.createOrUpdateInventory(request));
    }

    // ===================== GET INVENTORY BY PRODUCT =====================
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                service.getInventoryByProductId(productId)
        );
    }

    // ===================== INCREASE STOCK =====================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}/increase")
    public ResponseEntity<MessageResponse> increaseStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return new ResponseEntity<>(service.increaseStock(productId, quantity),HttpStatus.OK);
    }

    // ===================== DECREASE STOCK =====================
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{productId}/decrease")
    public ResponseEntity<MessageResponse> decreaseStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return new ResponseEntity<>(service.decreaseStock(productId, quantity),HttpStatus.OK);
    }

    // ===================== RESERVE STOCK (FOR CART / ORDER HOLD) =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{productId}/reserve")
    public ResponseEntity<MessageResponse> reserveStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return new ResponseEntity<>(service.reserveStock(productId, quantity),
                HttpStatus.OK);
    }

    // ===================== RELEASE RESERVED STOCK =====================
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{productId}/release")
    public ResponseEntity<MessageResponse> releaseStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        return new ResponseEntity<>(service.releaseReservedStock(productId, quantity),
                HttpStatus.OK);
    }

}