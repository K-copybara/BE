package org.example.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.order.dto.response.OrderHistoryResponse;
import org.example.domain.order.service.OrderHistoryService;
import org.example.dto.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    // 고객 주문 내역 조회
    @GetMapping
    public Response<OrderHistoryResponse> getCustomerOrders(
            @RequestParam Long storeId,
            @RequestParam String customerKey
    ) {
        OrderHistoryResponse response = orderHistoryService.getCustomerOrders(storeId, customerKey);
        return Response.success("주문 내역 조회 성공", response);
    }

}