package org.example.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StoreMenuResponseDto {
    private Long menuId;
    private String name;
    private String menuInfo;
    private Long price;
    private String status;
    private String imageUrl;
}
