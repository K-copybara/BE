package org.example.domain.cart.repository;

import org.example.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByStoreIdAndCustomerKey(Long storeId, String customerKey);
}
