package org.example.domain.stats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class WeekdaySalesResponse {
    private String weekday;
    private Long sales;
}