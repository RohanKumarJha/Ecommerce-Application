package ecommerce.product.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    private String productName;
    private String description;
    private String image;

    private BigDecimal price;
    private BigDecimal discount;

    private Long sellerId;
    private Long categoryId;

    public BigDecimal getSpecialPrice() {
        if (price == null) return BigDecimal.ZERO;
        if (discount == null) return price;
        return price.subtract(discount);
    }
}