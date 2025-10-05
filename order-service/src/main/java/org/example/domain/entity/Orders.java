package org.example.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@Builder
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 100)
    private String orderId;  // 토스와 통신하는 주문번호(UUID)

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "customer_key", nullable = false, length = 120)
    private String customerKey;

    @Column(length = 255)
    private String requestNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_price", nullable = false)
    private Long totalPrice = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TossPayment> tossPayments = new ArrayList<>();

    // 취소 상태 변경
    public void cancel() {
        this.orderStatus = OrderStatus.CANCELED;
    }

    // orderitem 추가
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrders(this); // 양방향 동기화
    }

    // 주문 완료
    public void complete() {
        this.orderStatus = OrderStatus.COMPLETED;
    }
}
