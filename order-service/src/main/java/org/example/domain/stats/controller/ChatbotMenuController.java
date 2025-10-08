package org.example.domain.stats.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.stats.dto.request.MenuSalesRequest;
import org.example.domain.stats.dto.request.Top3MenuRequest;
import org.example.domain.stats.service.MenuStatsService;
import org.example.dto.Response;
import org.example.dto.Top3Menu.Top3MenuResponseEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customer")
public class ChatbotMenuController {

    private final MenuStatsService menuStatsService;

    // 상위 3개
    @PostMapping("/top3-menus")
    public ResponseEntity<Response<Top3MenuResponseEvent>> getTop3Menus(
            @RequestBody Top3MenuRequest request
    ) throws Exception {
        Top3MenuResponseEvent event = menuStatsService.getTop3Menus(request.getStoreId());
        return ResponseEntity.ok(Response.success("한 달간 판매량 상위 3개 메뉴 조회 성공", event));
    }

    // 특정 메뉴 판매량
    @PostMapping("/menu-sales")
    public ResponseEntity<Response<Map<Long, Long>>> getMenuSales(
            @RequestBody MenuSalesRequest request
    ) {
        Map<Long, Long> result = menuStatsService.getMonthlySales(request.getMenuIds());
        return ResponseEntity.ok(Response.success("한 달간 메뉴 판매량 조회 성공", result));
    }
}
