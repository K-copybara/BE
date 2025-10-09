package org.example.domain.menu.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.menu.dto.response.CustomerMenuDetailResponseDto;
import org.example.domain.menu.dto.response.CustomerMenuListResponseDto;
import org.example.domain.menu.service.CustomerMenuService;
import org.example.dto.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer/store")
public class CustomerMenuController {

    private final CustomerMenuService customerMenuService;

    // 고객용 메뉴 상세 조회
    @GetMapping("/{storeId}/menus/{menuId}")
    public ResponseEntity<Response<CustomerMenuDetailResponseDto>> getMenuDetail(
            @PathVariable Long storeId,
            @PathVariable Long menuId
    ) {
        Response<CustomerMenuDetailResponseDto> response = customerMenuService.getMenuDetail(storeId, menuId);
        return ResponseEntity.ok(response);
    }

    // 전체 메뉴 조회
    @GetMapping("/{storeId}/menus")
    public ResponseEntity<Response<List<CustomerMenuListResponseDto>>> getAllMenusByStore(
            @PathVariable Long storeId
    ) {
        Response<List<CustomerMenuListResponseDto>> response = customerMenuService.getAllMenusByStore(storeId);
        return ResponseEntity.ok(response);
    }
}
