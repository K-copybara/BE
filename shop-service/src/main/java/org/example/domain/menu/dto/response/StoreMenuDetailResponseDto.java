package org.example.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoreMenuDetailResponseDto {

    private Long menuId;
    private String menuName;
    private Long menuPrice;
    private String menuInfo;
    private String menuPicture;
    private String menuStatus;   // "ON_SALE" / "SOLD_OUT"
    private Long spicyLevel;
    private List<String> allergies;
    private String extraInfo;
    private CategoryDto category;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDto {
        private Long categoryId;
        private String categoryName;
    }
}

