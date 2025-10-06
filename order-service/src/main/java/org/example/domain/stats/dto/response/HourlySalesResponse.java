package org.example.domain.stats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HourlySalesResponse {
    private String hour;       // "09", "10", "11" ...
    private Long sales;        // 시간대 총 매출액
    private Long orderCount;   // 주문 건수
}
