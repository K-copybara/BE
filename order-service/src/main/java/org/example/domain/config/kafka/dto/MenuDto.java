package org.example.domain.config.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuDto {
    private Long menuId;
    private String menuName;
    private Integer menuPrice;
}
