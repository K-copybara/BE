package org.example.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerMenuDetailResponseDto {
    private Long menuId;
    private String menuName;
    private Long menuPrice;
    private String menuInfo;
    private String menuPicture;
}

