package ecommerce.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    private Long orderId;

    private Long productId;

    // ✅ Snapshot fields (VERY IMPORTANT)
    private String productName;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal discount;

    private BigDecimal totalPrice;
}