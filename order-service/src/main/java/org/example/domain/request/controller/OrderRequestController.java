package org.example.domain.request.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.request.dto.request.OrderRequestDto;
import org.example.domain.request.dto.response.OrderRequestResponse;
import org.example.domain.request.service.OrderRequestService;
import org.example.dto.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/order-request")
@RequiredArgsConstructor
public class OrderRequestController {

    private final OrderRequestService orderRequestService;

    // 요청 사항 저장 api
    @PostMapping
    public Response<OrderRequestResponse> createOrderRequest(
            @RequestBody OrderRequestDto dto
    ) {
        OrderRequestResponse response = orderRequestService.createOrderRequest(dto);
        return Response.success("요청사항 저장 성공", response);
    }
}

