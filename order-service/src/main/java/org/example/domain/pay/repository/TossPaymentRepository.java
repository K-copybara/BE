package org.example.domain.pay.repository;


import org.example.domain.entity.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TossPaymentRepository extends JpaRepository<TossPayment, UUID> {
}
