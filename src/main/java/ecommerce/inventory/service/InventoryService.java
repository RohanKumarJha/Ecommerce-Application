package ecommerce.inventory.service;

import ecommerce.core.dto.response.MessageResponse;
import ecommerce.inventory.dto.request.InventoryRequest;
import ecommerce.inventory.dto.response.InventoryResponse;

public interface InventoryService {

    InventoryResponse createOrUpdateInventory(InventoryRequest request);

    InventoryResponse getInventoryByProductId(Long productId);

    MessageResponse increaseStock(Long productId, Integer quantity);

    MessageResponse decreaseStock(Long productId, Integer quantity);

    MessageResponse reserveStock(Long productId, Integer quantity);

    MessageResponse releaseReservedStock(Long productId, Integer quantity);
}