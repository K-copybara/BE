package org.example.domain.stats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MenuSalesResponse {

    private Long menuId;
    private String name;
    private Long sales;
    private Long orderCount;
    private Long reviewCount;

}

