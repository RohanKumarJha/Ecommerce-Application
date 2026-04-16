package ecommerce.product.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    private String productName;

    private String description;
    private String image;

    @NotNull(message = "Price is required")
    @Positive
    private BigDecimal price;

    @PositiveOrZero
    private BigDecimal discount;
}