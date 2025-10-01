package org.example.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_request_item")
public class OrderRequestItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrderRequest orderRequest;

    private String name;
    private Long amount;
}

