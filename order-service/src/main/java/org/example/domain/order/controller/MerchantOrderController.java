package org.example.domain.order.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.entity.OrderStatus;
import org.example.domain.order.dto.response.MerchantOrderSummaryDto;
import org.example.domain.order.service.MerchantOrderService;
import org.example.dto.Response;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/merchant/orders")
@RequiredArgsConstructor
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;

    // 사장 주문 내역 조회
    @GetMapping
    public Response<List<MerchantOrderSummaryDto>> getOrdersByStatus(
            @RequestHeader("X-Store-Id") Long storeId,
            @RequestParam(required = false) String status
    ) {
        OrderStatus orderStatus = (status != null)
                ? OrderStatus.valueOf(status.toUpperCase())
                : OrderStatus.PENDING; // 기본값
        List<MerchantOrderSummaryDto> response = merchantOrderService.getOrdersByStatus(storeId, orderStatus);
        return Response.success("주문 목록 조회 성공", response);
    }

    // 주문 완료

    @PostMapping("/{orderId}")
    public Response<Void> completeOrder(@PathVariable String orderId) {
        merchantOrderService.completeOrder(orderId);
        return Response.success("주문 완료");
    }

}
