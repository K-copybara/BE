package org.example.domain.store.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.store.dto.response.CustomerStoreResponseDto;
import org.example.domain.store.service.CustomerStoreService;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/store")
public class CustomerStoreController {

    private final CustomerStoreService customerStoreService;

    // 고객 가게 정보 조회
    @GetMapping("/{storeId}")
    public ResponseEntity<Response<CustomerStoreResponseDto>> getCustomerStoreInfo(
            @PathVariable Long storeId,
            @RequestParam Long tableId
    ) {
        Response<CustomerStoreResponseDto> response = customerStoreService.getCustomerStoreInfo(storeId, tableId);
        return ResponseEntity.ok(response);
    }
}
