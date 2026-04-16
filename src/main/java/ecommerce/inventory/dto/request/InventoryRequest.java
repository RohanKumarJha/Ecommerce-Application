package ecommerce.inventory.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {

    private Long productId;
    private Integer availableQuantity;
}