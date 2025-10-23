package org.example.domain.request.repository;

import org.example.domain.entity.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {

    // 고객 주문 내역 조회
    @Query("SELECT r FROM OrderRequest r LEFT JOIN FETCH r.items WHERE r.storeId = :storeId AND r.customerKey = :customerKey ORDER BY r.createdAt DESC")
    List<OrderRequest> findByStoreIdAndCustomerKey(@Param("storeId") Long storeId,
                                                   @Param("customerKey") String customerKey);

    // 요청시간 기준 내림차순
    List<OrderRequest> findAllByStoreIdOrderByCreatedAtDesc(Long storeId);

    List<OrderRequest> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end
    );

    List<OrderRequest> findByStoreIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long storeId, LocalDateTime start, LocalDateTime end
    );
}
