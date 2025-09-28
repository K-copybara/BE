package org.example.domain.pay.service;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.domain.entity.*;
import org.example.domain.pay.dto.request.ConfirmPaymentRequest;
import org.example.domain.pay.repository.OrdersRepository;
import org.example.domain.pay.repository.TossPaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

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
//    /**
//     * 결제 정보 저장
//     */
//    public TossPayment savePayment(ConfirmPaymentRequest request,
//                                   Orders orders,
//                                   TossPaymentMethod method,
//                                   TossPaymentStatus status,
//                                   LocalDateTime approvedAt) {
//
//        TossPayment tossPayment = TossPayment.builder()
//                .paymentId(UUID.randomUUID())              // PK
//                .orders(orders)                            // 주문 객체 (N:1 관계)
//                .tossOrderId(request.orderId())            // 토스 orderId
//                .tossPaymentKey(request.paymentKey())      // 토스 paymentKey
//                .tossPaymentMethod(method)                 // 카드, 가상계좌 등
//                .tossPaymentStatus(status)                 // APPROVED / CANCELED / FAILED
//                .requestedAt(LocalDateTime.now())
//                .approvedAt(approvedAt)
//                .totalAmount(request.amount().longValue())
//                .build();
//
//        return tossPaymentRepository.save(tossPayment);
//    }

}
