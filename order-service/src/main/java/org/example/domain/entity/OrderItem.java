package org.example.domain.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OrderItem : Order = N:1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_id", nullable = false)
    private Orders orders;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(name = "order_quantity", nullable = false)
    private Long orderQuantity;

    @Column(name = "total_menu_price", nullable = false)
    private Long totalMenuPrice = 0L;
}