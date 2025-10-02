package org.example.domain.request.repository;

import org.example.domain.entity.OrderRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {

}
