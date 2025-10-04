package org.example.domain.order.repository;


import org.example.domain.entity.OrderStatus;
import org.example.domain.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}