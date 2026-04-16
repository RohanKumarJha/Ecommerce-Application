package ecommerce.order.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private Long addressId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDate orderDate;
    private List<OrderItemResponse> items;
}