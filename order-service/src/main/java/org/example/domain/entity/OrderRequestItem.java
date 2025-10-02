package org.example.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "order_request_item")
@Getter
@Builder
public class OrderRequestItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_request_id", nullable = false)
    private OrderRequest orderRequest;

    private Long menuId;
    private Long amount;

    public void setOrderRequest(OrderRequest orderRequest) {
        this.orderRequest = orderRequest;
    }
}

