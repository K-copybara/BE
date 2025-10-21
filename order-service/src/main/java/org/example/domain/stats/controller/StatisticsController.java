package org.example.domain.stats.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.stats.dto.response.*;
import org.example.domain.stats.service.StatisticsService;
import org.example.dto.Response;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    // 월별 일별 매출
    @GetMapping("/daily")
    public Response<List<DailySalesDto>> getDailySales(
            @RequestParam("month") String month,
            @RequestHeader("X-Store-Id") Long storeId
    ) {
        List<DailySalesDto> data = statisticsService.getDailySales(storeId, month);
        return Response.success("월별 일별 매출 조회 성공", data);
    }

    // 월별 요일별 매출
    @GetMapping("/weekday")
    public Response<List<WeekdaySalesResponse>> getWeekdaySales(
            @RequestParam String month,
            @RequestParam(required = false) Long storeId // 개발용: 나중엔 토큰에서 추출
    ) {
        List<WeekdaySalesResponse> data = statisticsService.getWeekdaySales(month, storeId);
        return Response.success("월별 요일별 매출 조회 성공", data);
    }


    // 일별 매출, 주문 건수
    @GetMapping("/daily/order")
    public Response<DailySalesResponse> getDailyOrderSales(
            @RequestParam String date,
            @RequestParam(required = false) Long storeId // 나중에 토큰에서 추출 가능
    ) {
        DailySalesResponse data = statisticsService.getDailySales(date, storeId);
        return Response.success("일별 매출 및 주문 건수 조회 성공", data);
    }

    // 일별 시간대별 매출
    @GetMapping("/hourly")
    public Response<List<HourlySalesResponse>> getHourlySales(
            @RequestParam String date,
            @RequestParam(required = false) Long storeId // 나중에 JWT에서 추출
    ) {
        List<HourlySalesResponse> data = statisticsService.getHourlySales(date, storeId);
        return Response.success("일별 시간대별 매출 조회 성공", data);
    }

    // 일별 메뉴 매출 조회
    @GetMapping("/menu")
    public Response<?> getMenuSales(
            @RequestParam String date,
            @RequestParam(required = false, defaultValue = "sales") String sort,
            @RequestParam Long storeId
    ) {
        List<MenuSalesResponse> result = statisticsService.getMenuSales(date, sort, storeId);
        return Response.success("일별 메뉴 매출 조회 성공", result);
    }
}
