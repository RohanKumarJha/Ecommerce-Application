package ecommerce.order.model;

import ecommerce.order.model.ENUM.OrderStatus;
import ecommerce.order.state.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private Long userId;
    private Long addressId;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDate orderDate;

    // ❗ State is not stored in DB
    @Transient
    private OrderState state;

    // ✅ Auto set values
    @PrePersist
    public void prePersist() {
        this.orderDate = LocalDate.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }

    // ✅ Initialize state based on status
    public void initState() {
        switch (this.status) {
            case PENDING -> this.state = new PendingState();
            case CONFIRMED -> this.state = new ConfirmedState();
            case SHIPPED -> this.state = new ShippedState();
            case DELIVERED -> this.state = new DeliveredState();
            case CANCELLED -> this.state = new CancelledState();
            default -> throw new IllegalStateException("Unknown status: " + this.status);
        }
    }

    // ✅ Move to next state
    public void next() {
        if (state == null) initState();
        state.next(this);
        this.status = state.getStatus();
    }

    // ❌ Cancel order
    public void cancel() {
        if (state == null) initState();
        state.cancel(this);
        this.status = state.getStatus();
    }
}