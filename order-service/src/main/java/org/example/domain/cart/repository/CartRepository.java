package org.example.domain.cart.repository;

import org.example.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // customerKey로 장바구니 조회 (비회원 결제 식별용)
    Optional<Cart> findByCustomerKey(String customerKey);

    // 특정 storeId와 customerKey 조합으로 조회
    Optional<Cart> findByStoreIdAndCustomerKey(Long storeId, String customerKey);
}
