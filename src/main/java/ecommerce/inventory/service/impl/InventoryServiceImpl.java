package ecommerce.inventory.service.impl;

import ecommerce.core.dto.response.MessageResponse;
import ecommerce.core.exception.APIException;
import ecommerce.core.exception.ResourceNotFoundException;
import ecommerce.inventory.dto.request.InventoryRequest;
import ecommerce.inventory.dto.response.InventoryResponse;
import ecommerce.inventory.model.Inventory;
import ecommerce.inventory.repository.InventoryRepository;
import ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public InventoryResponse createOrUpdateInventory(InventoryRequest request) {
        log.debug("Create/Update inventory for productId={}", request.getProductId());
        if (request.getProductId() == null) {
            throw new APIException("ProductId must not be null");
        }
        Inventory inventory = inventoryRepository
                .findByProductId(request.getProductId())
                .orElseGet(() -> {
                    log.debug("Inventory not found, creating new one for productId={}", request.getProductId());
                    return Inventory.builder()
                            .productId(request.getProductId())
                            .availableQuantity(0)
                            .reservedQuantity(0)
                            .soldQuantity(0)
                            .build();
                });
        if (request.getAvailableQuantity() != null) {
            inventory.setAvailableQuantity(request.getAvailableQuantity());
        }
        Inventory saved = inventoryRepository.save(inventory);
        log.info("Inventory saved successfully for productId={}", saved.getProductId());
        return InventoryResponse.builder()
                .productId(saved.getProductId())
                .availableQuantity(saved.getAvailableQuantity())
                .reservedQuantity(saved.getReservedQuantity())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {
        log.debug("Fetching inventory for productId={}", productId);
        if (productId == null) {
            throw new APIException("ProductId must not be null");
        }
        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory", "productId", productId
                ));
        log.info("Inventory found for productId={}", productId);
        return InventoryResponse.builder()
                .productId(inventory.getProductId())
                .availableQuantity(inventory.getAvailableQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .build();
    }

    @Override
    @Transactional
    public MessageResponse increaseStock(Long productId, Integer quantity) {
        log.debug("Increasing stock for productId={}, quantity={}", productId, quantity);
        if (productId == null || quantity == null || quantity <= 0) {
            throw new APIException("ProductId and quantity must be valid");
        }
        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory", "productId", productId
                ));
        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + quantity
        );
        inventoryRepository.save(inventory);
        log.info("Stock increased for productId={}, added={}", productId, quantity);
        return new MessageResponse("Stock increased successfully");
    }

    @Override
    @Transactional
    public MessageResponse decreaseStock(Long productId, Integer quantity) {
        log.debug("Decreasing stock for productId={}, quantity={}", productId, quantity);
        if (productId == null || quantity == null || quantity <= 0) {
            throw new APIException("ProductId and quantity must be valid");
        }
        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory", "productId", productId
                ));
        if (inventory.getAvailableQuantity() < quantity) {
            throw new APIException("Insufficient stock for productId: " + productId);
        }
        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() - quantity
        );
        inventory.setSoldQuantity(
                (inventory.getSoldQuantity() == null ? 0 : inventory.getSoldQuantity()) + quantity
        );
        inventoryRepository.save(inventory);
        log.info("Stock decreased for productId={}, removed={}", productId, quantity);
        return new MessageResponse("Stock decreased successfully");
    }

    @Override
    @Transactional
    public MessageResponse reserveStock(Long productId, Integer quantity) {
        log.debug("Reserving stock for productId={}, quantity={}", productId, quantity);
        if (productId == null || quantity == null || quantity <= 0) {
            throw new APIException("ProductId and quantity must be valid");
        }
        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory", "productId", productId
                ));
        int available = inventory.getAvailableQuantity() == null ? 0 : inventory.getAvailableQuantity();
        int reserved = inventory.getReservedQuantity() == null ? 0 : inventory.getReservedQuantity();
        if (available < quantity) {
            throw new APIException("Not enough stock to reserve for productId: " + productId);
        }
        inventory.setAvailableQuantity(available - quantity);
        inventory.setReservedQuantity(reserved + quantity);
        inventoryRepository.save(inventory);
        log.info("Stock reserved for productId={}, quantity={}", productId, quantity);
        return new MessageResponse("Stock reserved successfully");
    }

    @Override
    @Transactional
    public MessageResponse releaseReservedStock(Long productId, Integer quantity) {
        log.debug("Releasing reserved stock for productId={}, quantity={}", productId, quantity);
        if (productId == null || quantity == null || quantity <= 0) {
            throw new APIException("ProductId and quantity must be valid");
        }
        Inventory inventory = inventoryRepository
                .findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory", "productId", productId
                ));
        int reserved = inventory.getReservedQuantity() == null ? 0 : inventory.getReservedQuantity();
        int available = inventory.getAvailableQuantity() == null ? 0 : inventory.getAvailableQuantity();
        if (reserved < quantity) {
            throw new APIException("Not enough reserved stock to release for productId: " + productId);
        }
        inventory.setReservedQuantity(reserved - quantity);
        inventory.setAvailableQuantity(available + quantity);
        inventoryRepository.save(inventory);
        log.info("Reserved stock released for productId={}, quantity={}", productId, quantity);
        return new MessageResponse("Reserved stock released successfully");
    }
}
