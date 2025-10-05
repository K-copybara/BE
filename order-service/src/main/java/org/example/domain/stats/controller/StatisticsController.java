package org.example.domain.stats.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.stats.dto.response.DailySalesDto;
import org.example.domain.stats.dto.response.DailySalesResponse;
import org.example.domain.stats.dto.response.WeekdaySalesResponse;
import org.example.domain.stats.service.StatisticsService;
import org.example.dto.Response;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchant/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    // 월별 일별 매출
    @GetMapping("/daily")
    public Response<List<DailySalesDto>> getDailySales(
            @RequestParam("storeId") Long storeId,
            @RequestParam("month") String month
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
    public Response<DailySalesResponse> getDailySales(
            @RequestParam String date,
            @RequestParam(required = false) Long storeId // 나중에 토큰에서 추출 가능
    ) {
        DailySalesResponse data = statisticsService.getDailySales(date, storeId);
        return Response.success("일별 매출 및 주문 건수 조회 성공", data);
    }
}
