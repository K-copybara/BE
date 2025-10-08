package org.example.domain.order.repository;


import org.example.domain.entity.OrderStatus;
import org.example.domain.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByOrderId(String orderId);

    // 고객 주문내역 조회
    @Query("""
                SELECT DISTINCT o
                FROM Orders o
                LEFT JOIN FETCH o.orderItems i
                WHERE o.storeId = :storeId
                  AND o.customerKey = :customerKey
                ORDER BY o.createdAt DESC
            """)
    List<Orders> findOrdersWithItemsByCustomer(
            @Param("storeId") Long storeId,
            @Param("customerKey") String customerKey
    );

    // 사장 주문내역 조회

    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.orderItems " +
            "WHERE o.storeId = :storeId AND o.orderStatus = :status " +
            "ORDER BY o.createdAt DESC")
    List<Orders> findOrdersByStoreAndStatus(@Param("storeId") Long storeId,
                                            @Param("status") OrderStatus status);

    // 월별 요일별 매출
    List<Orders> findByStoreIdAndOrderStatusAndCreatedAtBetween(
            Long storeId,
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    // 상위 3개 메뉴 (요청사항 제외)
    @Query("""
                SELECT oi.menuId
                FROM OrderItem oi
                JOIN oi.orders o
                WHERE o.storeId = :storeId
                  AND o.orderStatus = 'COMPLETED'
                  AND o.createdAt >= :startDate
                  AND oi.menuCategory <> '요청사항'
                GROUP BY oi.menuId
                ORDER BY SUM(oi.orderQuantity) DESC
                LIMIT 3
            """)
    List<Long> findTop3MenuIdsByStoreAndDate(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDateTime startDate
    );
}