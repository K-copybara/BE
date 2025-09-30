package org.example.domain.pay.service;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.domain.entity.*;
import org.example.domain.pay.repository.OrdersRepository;
import org.example.domain.pay.repository.TossPaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TossPaymentRepository tossPaymentRepository;
    private final HttpSession session;
    private final OrdersRepository ordersRepository;

    public Orders createPayment(String orderId, Long customerKey, int amount) {
        Orders orders = Orders.builder()
                .orderId(orderId)                 // 토스 orderId
                .customerKey(customerKey)
                .totalPrice((long) amount)
                .tableId(0L)
                .orderStatus(OrderStatus.PENDING) // 기본 상태
                .createdAt(LocalDateTime.now())
                .request("결제")           // 임시 값
                .build();

        return ordersRepository.save(orders);
    }

}
