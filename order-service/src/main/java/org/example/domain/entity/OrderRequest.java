package org.example.domain.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "order_request")
public class OrderRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;
    private Long tableId;

    private String requestNote;

    @OneToMany(mappedBy = "orderRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderRequestItem> items;
}

