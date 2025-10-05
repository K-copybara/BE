package org.example.domain.stats.controller;

import lombok.RequiredArgsConstructor;
import org.example.domain.stats.dto.request.DailySalesDto;
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
}
