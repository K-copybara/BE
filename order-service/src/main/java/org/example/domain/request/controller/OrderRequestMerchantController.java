package org.example.domain.request.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.request.dto.response.OrderRequestMerchantResponse;
import org.example.domain.request.service.OrderRequestService;
import org.example.dto.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/orders")
public class OrderRequestMerchantController {

    private final OrderRequestService orderRequestService;

    // 사장 요청 조회
    @GetMapping("/requests")
    public Response<List<OrderRequestMerchantResponse>> getRequests(
            @RequestParam("storeId") Long storeId   // 개발용
    ) {
        List<OrderRequestMerchantResponse> data = orderRequestService.getRequestsByStore(storeId);
        return Response.success("요청 목록 조회 성공", data);
    }
}
