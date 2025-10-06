package org.example.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "customer_key", nullable = false, length = 120)
    private String customerKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CartStatus status = CartStatus.ACTIVE;

    // Cart 1 : N CartItem
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items;

    // 결제 완료 시 상태 변경
    public void markAsCompleted() {
        this.status = CartStatus.COMPLETED;
    }

    // 새로운 장바구니 초기화용 팩토리
    public static Cart newActiveCart(Long storeId, String customerKey) {
        return Cart.builder()
                .storeId(storeId)
                .customerKey(customerKey)
                .status(CartStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();
    }
}
