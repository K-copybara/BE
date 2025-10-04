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

    @Column(name = "menu_name", nullable = false, length = 255)
    private String menuName;

    @Column(name = "menu_price", nullable = false)
    private Long menuPrice;

    @Column(name = "menu_category", nullable = false, length = 100)
    private String menuCategory;

    @Column(name = "order_quantity", nullable = false)
    private Long orderQuantity;

    @Column(name = "total_menu_price", nullable = false)
    private Long totalMenuPrice = 0L;

    public void setOrders(Orders orders) {
        this.orders = orders;
    }
}