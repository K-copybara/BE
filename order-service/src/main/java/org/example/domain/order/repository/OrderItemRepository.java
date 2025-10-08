package org.example.domain.order.repository;

import org.example.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 특정 메뉴 판매량
    @Query("""
        SELECT oi.menuId, SUM(oi.orderQuantity)
        FROM OrderItem oi
        JOIN oi.orders o
        WHERE o.orderStatus = 'COMPLETED'
          AND o.createdAt >= :startDate
          AND oi.menuId IN :menuIds
        GROUP BY oi.menuId
    """)
    List<Object[]> findMenuSalesByMenuIds(
            @Param("menuIds") List<Long> menuIds,
            @Param("startDate") LocalDateTime startDate
    );
}
