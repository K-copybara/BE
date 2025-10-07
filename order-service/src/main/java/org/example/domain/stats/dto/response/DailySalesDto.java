package org.example.domain.stats.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesDto {
    private String date;  // yyyy-MM-dd
    private Long sales;
}
