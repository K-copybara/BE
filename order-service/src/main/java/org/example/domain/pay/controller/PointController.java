package org.example.domain.pay.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.entity.Orders;
import org.example.domain.order.repository.OrdersRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PointController {

    @Value("${payment.client.key}")
    private String CLIENT_KEY;

    private final OrdersRepository ordersRepository;

    /**
     * 결제 페이지 호출 (checkout.html 렌더링)
     * 예: http://localhost:8080/v1/payments/{orderId}
     */
    @GetMapping("/{orderId}")
    public String getPaymentPage(@PathVariable String orderId, Model model) {
        // DB에서 orderId 조회
        Orders orders = ordersRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // checkout.html에서 사용할 데이터 세팅
        model.addAttribute("clientKey", CLIENT_KEY);
        model.addAttribute("amount", orders.getTotalPrice());
        model.addAttribute("orderId", orders.getOrderId());
        model.addAttribute("customerKey", orders.getCustomerKey());

        return "payment/checkout"; // → templates/payment/checkout.html
    }

}
