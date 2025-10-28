package org.example.domain.pay.repository;


import org.example.domain.entity.Orders;
import org.example.domain.entity.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TossPaymentRepository extends JpaRepository<TossPayment, UUID> {
    Optional<TossPayment> findByTossPaymentKey(String tossPaymentKey);

    // 특정 주문(Orders)으로 결제 정보 조회
    Optional<TossPayment> findByOrders(Orders orders);
}
