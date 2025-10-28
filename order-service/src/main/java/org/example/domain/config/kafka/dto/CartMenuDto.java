package org.example.domain.config.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartMenuDto {
    private Long menuId;
    private String menuName;
    private Long menuPrice;
    private String menuCategory;
    private String menuPicture;
}
