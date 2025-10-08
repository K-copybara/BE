package org.example.domain.customer.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.customer.dto.request.CustomerSessionRequest;
import org.example.domain.customer.dto.response.CustomerSessionResponse;
import org.example.domain.customer.service.CustomerSessionService;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
public class CustomerSessionController {

    private final CustomerSessionService customerSessionService;

    // customerKey 생성
    @PostMapping("/session")
    public ResponseEntity<Response<CustomerSessionResponse>> createSession(
            @RequestBody CustomerSessionRequest request
    ) {
        CustomerSessionResponse response = customerSessionService.createSession(request.getStoreId(), request.getTableId());
        return ResponseEntity.ok(Response.success("고객 세션 생성 성공", response));
    }
}

