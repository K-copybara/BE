package org.example.domain.store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StoreCategorySimpleResponseDto {
    private Long categoryId;
    private String categoryName;
}
