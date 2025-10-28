package org.example.domain.stats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DailySalesResponse {
    private String date;   // "2025-09-09"
    private Long sales;    // 총 매출
    private Long orders;    // 주문 건수
}