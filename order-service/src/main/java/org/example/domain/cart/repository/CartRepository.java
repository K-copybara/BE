package org.example.domain.cart.repository;

import org.example.domain.entity.Cart;
import org.example.domain.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // 기본 장바구니 조회 (상점 + 고객 기준)
    Optional<Cart> findByStoreIdAndCustomerKey(Long storeId, String customerKey);

    // 활성 장바구니 조회 (상점 + 고객 + ACTIVE 상태)
    Optional<Cart> findByStoreIdAndCustomerKeyAndStatus(Long storeId, String customerKey, CartStatus status);
}
