package org.example.domain.menu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerMenuListResponseDto {

    private Long menuId;
    private String menuName;
    private String menuInfo;
    private Long menuPrice;
    private String menuPicture;
    private CategoryDto category;

    // 고객용 단일 응답 객체 ->  내부 정적 클래스 (OOP)
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDto {
        private Long categoryId;
        private String categoryName;
    }
}

