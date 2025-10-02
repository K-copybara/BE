package org.example.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_request")
@Getter
@Builder
public class OrderRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;
    private Long tableId;
    private String customerKey;

    private String requestNote;

    @OneToMany(mappedBy = "orderRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderRequestItem> items = new ArrayList<>();

    public void addItem(OrderRequestItem item) {
        items.add(item);
        item.setOrderRequest(this);
    }
}

